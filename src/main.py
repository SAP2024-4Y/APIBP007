import os, sqlite3, json, base64, math, time, threading
from datetime import datetime, timezone, timedelta
from pathlib import Path
from collections import defaultdict
import jwt
import yaml
from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import JSONResponse, HTMLResponse
from fastapi.openapi.docs import get_swagger_ui_html
from graphql import build_schema, graphql_sync, GraphQLError
from pydantic import BaseModel

ROOT = Path(__file__).resolve().parent.parent
DB = os.getenv('DB_PATH', str(ROOT / 'dine.db'))
SECRET = os.getenv('JWT_SECRET', 'local-demo-secret-change-before-deployment')
ERRORS = {'BAD_INPUT':400,'UNAUTHENTICATED':401,'FORBIDDEN':403,'NOT_FOUND':404,'CONFLICT':409,'INVALID_WINDOW':422,'INVALID_CURSOR':400,'INVALID_LOCATION':422,'INVALID_PRICE':422,'RATE_LIMITED':429,'INTERNAL_ERROR':500}
class Domain(Exception):
    def __init__(self, code): self.code = code

def db():
    c = sqlite3.connect(DB, timeout=10); c.row_factory = sqlite3.Row; return c

def seed():
    with db() as c:
        c.executescript('CREATE TABLE IF NOT EXISTS restaurants(id INTEGER PRIMARY KEY,name TEXT,city TEXT,category TEXT,status TEXT,price INTEGER,latitude REAL,longitude REAL); CREATE TABLE IF NOT EXISTS slots(id INTEGER PRIMARY KEY,restaurant_id INTEGER REFERENCES restaurants(id),starts_at TEXT,ends_at TEXT); CREATE TABLE IF NOT EXISTS bookings(id INTEGER PRIMARY KEY,slot_id INTEGER UNIQUE REFERENCES slots(id),username TEXT);')
        fresh=not c.execute('SELECT 1 FROM restaurants LIMIT 1').fetchone()
        cities=[('Chennai',13.0827,80.2707),('Bengaluru',12.9716,77.5946),('Mumbai',19.076,72.8777),('Delhi',28.6139,77.209),('Hyderabad',17.385,78.4867)]
        names=['Banana Leaf','Spice Courtyard','Tandoor House','Coastal Kitchen','Curry Junction','Royal Thali','Biryani Bazaar','Ocean Spice','Filter Coffee Kitchen','Saffron Table']; cats=['South Indian','North Indian','Biryani','Seafood','South Indian','North Indian','Biryani','Seafood','South Indian','North Indian']
        for i,(city,lat,lon) in enumerate(cities):
            for j,name in enumerate(names):
                rid=i*4+j+1 if j<4 else 21+i*6+j-4
                if fresh: c.execute('INSERT OR IGNORE INTO restaurants VALUES(?,?,?,?,?,?,?,?)',(rid,name+' '+city,city,cats[j],'OPEN',300+j*200+i*50,lat+(j%5)*.009,lon+(j//5)*.012))
                if not c.execute("SELECT id FROM restaurants WHERE id=?",(rid,)).fetchone(): continue
                for d in range(31):
                    day=datetime.now(timezone.utc).date()+timedelta(days=d)
                    for k,h in enumerate([7,13,15]):
                        start=f'{day}T{h:02}:00:00Z'; end=f'{day}T{h+1:02}:00:00Z'
                        c.execute('INSERT OR IGNORE INTO slots VALUES(?,?,?,?)',(int(day.strftime('%Y%m%d'))*1000+rid*10+k,rid,start,end))
seed()
app=FastAPI(docs_url=None,redoc_url=None,openapi_url=None)

def rows(sql,args=()):
    with db() as c: return [dict(r) for r in c.execute(sql,args)]
def restaurant(r): return dict(r,location={'type':'Point','coordinates':[r['longitude'],r['latitude']]})
def cursor(v): return base64.urlsafe_b64encode(json.dumps(v).encode()).decode()
def listing(status=None,category=None,min_price=None,max_price=None,sort='id',order='asc',limit=10,after=None,city=None):
    if sort not in ['id','name','price','category','status','city'] or order not in ['asc','desc'] or not 1<=limit<=100: raise Domain('BAD_INPUT')
    if (min_price is not None and min_price<0) or (max_price is not None and max_price<0) or (min_price is not None and max_price is not None and min_price>max_price): raise Domain('INVALID_PRICE')
    clauses=[]; args=[]
    for field,op,val in [('city','=',city),('status','=',status),('category','=',category),('price','>=',min_price),('price','<=',max_price)]:
        if val is not None: clauses.append(f'{field} {op} ?'); args.append(val)
    if after:
        try:
            s,o,v,i=json.loads(base64.urlsafe_b64decode(after)); assert (s,o)==(sort,order); assert isinstance(i,int)
        except Exception: raise Domain('INVALID_CURSOR')
        op='>' if order=='asc' else '<'; clauses.append(f'({sort} {op} ? OR ({sort} = ? AND id {op} ?))'); args.extend([v,v,i])
    result=rows('SELECT * FROM restaurants WHERE '+(' AND '.join(clauses) or '1=1')+f' ORDER BY {sort} {order}, id {order} LIMIT ?',args+[limit+1]); more=len(result)>limit; result=result[:limit]
    edges=[{'node':restaurant(r),'cursor':cursor([sort,order,r[sort],r['id']])} for r in result]; end=edges[-1]['cursor'] if edges else None
    return {'items':[e['node'] for e in edges],'edges':edges,'has_more':more,'after':end,'pageInfo':{'hasNextPage':more,'endCursor':end}}

def map_search(latitude=None,longitude=None,radius_km=None,min_lat=None,min_lon=None,max_lat=None,max_lon=None):
    a=[latitude,longitude,radius_km]; b=[min_lat,min_lon,max_lat,max_lon]; result=[]
    if any(x is not None for x in a):
        if any(x is None for x in a) or not (-90<=latitude<=90 and -180<=longitude<=180 and radius_km>=0): raise Domain('INVALID_LOCATION')
    if any(x is not None for x in b):
        if any(x is None for x in b) or not (-90<=min_lat<=max_lat<=90 and -180<=min_lon<=max_lon<=180): raise Domain('INVALID_LOCATION')
    for r in rows('SELECT * FROM restaurants'):
        lat,lon=r['latitude'],r['longitude']
        if latitude is not None:
            q=math.sin(math.radians(lat-latitude)/2)**2+math.cos(math.radians(lat))*math.cos(math.radians(latitude))*math.sin(math.radians(lon-longitude)/2)**2
            if 6371*2*math.asin(min(1,math.sqrt(q)))>radius_km: continue
        if min_lat is not None and not(min_lat<=lat<=max_lat and min_lon<=lon<=max_lon): continue
        result.append(restaurant(r))
    return result

def utc(s):
    try:
        d=datetime.fromisoformat(s.replace('Z','+00:00')); assert d.utcoffset()==timedelta(0); return d.strftime('%Y-%m-%dT%H:%M:%SZ')
    except Exception: raise Domain('INVALID_WINDOW')
def calendar(starts_at=None,ends_at=None,restaurant_id=None):
    a=utc(starts_at) if starts_at else '0000'; b=utc(ends_at) if ends_at else '9999'
    if a>=b: raise Domain('INVALID_WINDOW')
    return [dict(r,available=bool(r['available'])) for r in rows('SELECT s.*, CASE WHEN b.id IS NULL THEN 1 ELSE 0 END AS available FROM slots s LEFT JOIN bookings b ON b.slot_id=s.id WHERE s.starts_at<? AND s.ends_at>? AND (? IS NULL OR s.restaurant_id=?) ORDER BY s.starts_at,s.id',(b,a,restaurant_id,restaurant_id))]
def auth(request, role="customer"):
    try:
        scheme,token=request.headers.get('Authorization','').split(); assert scheme.lower()=='bearer'; user=jwt.decode(token,SECRET,algorithms=['HS256']); assert user['sub']
    except Exception: raise Domain('UNAUTHENTICATED')
    if user.get('role')!=role: raise Domain('FORBIDDEN')
    return user['sub']
def parse_slot_id(value):
    try: return int(value)
    except (ValueError,TypeError): raise Domain('BAD_INPUT')
def book(slot_id,request):
    user=auth(request)
    with db() as c:
        c.execute('BEGIN IMMEDIATE')
        if not c.execute('SELECT id FROM slots WHERE id=?',(slot_id,)).fetchone(): raise Domain('NOT_FOUND')
        try: i=c.execute('INSERT INTO bookings(slot_id,username) VALUES(?,?)',(slot_id,user)).lastrowid
        except sqlite3.IntegrityError: raise Domain('CONFLICT')
    return {'id':i,'slot_id':slot_id,'username':user}
class Credentials(BaseModel):
    username:str
    password:str
class Booking(BaseModel):
    slot_id:int
@app.exception_handler(Domain)
async def domain_error(request,exc): return JSONResponse({'code':exc.code,'message':exc.code.replace('_',' ').title()},status_code=ERRORS[exc.code])
from fastapi.exceptions import RequestValidationError
@app.exception_handler(RequestValidationError)
async def validation(request,exc): return JSONResponse({'code':'BAD_INPUT','message':'Invalid request'},status_code=400)
@app.exception_handler(Exception)
async def unexpected(request,exc): return JSONResponse({'code':'INTERNAL_ERROR','message':'Internal error'},status_code=500)
counts=defaultdict(int); lock=threading.Lock()
@app.middleware('http')
async def rate(request,call_next):
    now=int(time.time()); key=(request.client.host if request.client else 'local',now//60)
    with lock:
        for old in list(counts):
            if old[1]!=key[1]: del counts[old]
        counts[key]+=1; n=counts[key]
    headers={'RateLimit-Limit':'120','RateLimit-Remaining':str(max(0,120-n)),'RateLimit-Reset':str(60-now%60)}
    if n>120:
        headers['Retry-After']=headers['RateLimit-Reset']; body={'code':'RATE_LIMITED'}
        if request.url.path=='/graphql': body={'errors':[{'message':'Rate limited','extensions':dict(body,**headers)}]}
        return JSONResponse(body,status_code=429,headers=headers)
    response=await call_next(request); response.headers.update(headers); return response
@app.get('/health')
def health(): return {'status':'ok'}
@app.post('/api/v1/auth/token')
def token(body:Credentials):
    return issue_token(body.username, body.password)
def issue_token(username,password):
    if (username,password) not in [('demo','demo-password'),('admin','admin-password')]: raise Domain('UNAUTHENTICATED')
    return {'token':jwt.encode({'sub':username,'role':{'demo':'customer','admin':'admin'}[username],'exp':int(time.time())+3600},SECRET,algorithm='HS256'),'token_type':'Bearer','expires_in':3600}
@app.get('/api/v1/restaurants')
def list_api(status:str=None,category:str=None,min_price:float=None,max_price:float=None,sort:str='id',order:str='asc',limit:int=10,after:str=None,city:str=None): return listing(status,category,min_price,max_price,sort,order,limit,after,city)
@app.get('/api/v1/restaurants/map')
def map_api(latitude:float=None,longitude:float=None,radius_km:float=None,min_lat:float=None,min_lon:float=None,max_lat:float=None,max_lon:float=None): return map_search(latitude,longitude,radius_km,min_lat,min_lon,max_lat,max_lon)
@app.get('/api/v1/calendar')
def calendar_api(starts_at:str=None,ends_at:str=None,restaurant_id:int=None): return calendar(starts_at,ends_at,restaurant_id)
@app.post('/api/v1/bookings',status_code=201)
def booking_api(body:Booking,request:Request): return {'created':book(body.slot_id,request)}

class RestaurantInput(BaseModel):
    name:str
    city:str
    category:str
    status:str='OPEN'
    price:int
    latitude:float
    longitude:float
class RestaurantPatch(BaseModel):
    name:str|None=None
    city:str|None=None
    category:str|None=None
    status:str|None=None
    price:int|None=None
    latitude:float|None=None
    longitude:float|None=None
class SlotInput(BaseModel):
    restaurant_id:int
    starts_at:str
    ends_at:str

def validate_restaurant(v):
    if not all(v[k].strip() for k in ('name','city','category')) or v['status'] not in ('OPEN','CLOSED'): raise Domain('BAD_INPUT')
    if v['price']<0: raise Domain('INVALID_PRICE')
    if not (-90<=v['latitude']<=90 and -180<=v['longitude']<=180): raise Domain('INVALID_LOCATION')
def get_restaurant(id):
    r=rows('SELECT * FROM restaurants WHERE id=?',(id,))
    if not r: raise Domain('NOT_FOUND')
    return restaurant(r[0])
def add_restaurant(v,request):
    auth(request,'admin'); validate_restaurant(v)
    with db() as c:
        i=c.execute('INSERT INTO restaurants(name,city,category,status,price,latitude,longitude) VALUES(?,?,?,?,?,?,?)',tuple(v[k] for k in ('name','city','category','status','price','latitude','longitude'))).lastrowid
    return get_restaurant(i)
def update_restaurant(id,v,request):
    auth(request,'admin'); old=get_restaurant(id); old.update({k:x for k,x in v.items() if x is not None}); validate_restaurant(old)
    with db() as c: c.execute('UPDATE restaurants SET name=?,city=?,category=?,status=?,price=?,latitude=?,longitude=? WHERE id=?',tuple(old[k] for k in ('name','city','category','status','price','latitude','longitude'))+(id,))
    return get_restaurant(id)
def delete_restaurant(id,request):
    auth(request,'admin')
    with db() as c:
        c.execute('BEGIN IMMEDIATE')
        if not c.execute('SELECT id FROM restaurants WHERE id=?',(id,)).fetchone(): raise Domain('NOT_FOUND')
        if c.execute('SELECT b.id FROM bookings b JOIN slots s ON b.slot_id=s.id WHERE s.restaurant_id=?',(id,)).fetchone(): raise Domain('CONFLICT')
        c.execute('DELETE FROM slots WHERE restaurant_id=?',(id,)); c.execute('DELETE FROM restaurants WHERE id=?',(id,))
    return {'id':id}
def add_slot(v,request):
    auth(request,'admin'); a=utc(v['starts_at']); b=utc(v['ends_at']); rid=v['restaurant_id']
    if a>=b: raise Domain('INVALID_WINDOW')
    with db() as c:
        c.execute('BEGIN IMMEDIATE')
        if not c.execute('SELECT id FROM restaurants WHERE id=?',(rid,)).fetchone(): raise Domain('NOT_FOUND')
        if c.execute('SELECT id FROM slots WHERE restaurant_id=? AND starts_at<? AND ends_at>?',(rid,b,a)).fetchone(): raise Domain('CONFLICT')
        i=c.execute('SELECT COALESCE(MAX(id),0)+1 FROM slots WHERE id<1000000000').fetchone()[0]
        c.execute('INSERT INTO slots VALUES(?,?,?,?)',(i,rid,a,b))
    return {'id':i,'restaurant_id':rid,'starts_at':a,'ends_at':b,'available':True}
def my_bookings(request):
    return rows('SELECT * FROM bookings WHERE username=? ORDER BY id',(auth(request),))
def cancel_booking(id,request):
    user=auth(request)
    with db() as c:
        c.execute('BEGIN IMMEDIATE'); r=c.execute('SELECT * FROM bookings WHERE id=?',(id,)).fetchone()
        if not r: raise Domain('NOT_FOUND')
        if r['username']!=user: raise Domain('FORBIDDEN')
        c.execute('DELETE FROM bookings WHERE id=?',(id,))
    return {'id':id}
@app.get('/api/v1/restaurants/{id}')
def detail_api(id:int): return get_restaurant(id)
@app.post('/api/v1/restaurants',status_code=201)
def add_restaurant_api(body:RestaurantInput,request:Request): return {'created':add_restaurant(body.model_dump(),request)}
@app.patch('/api/v1/restaurants/{id}')
def update_restaurant_api(id:int,body:RestaurantPatch,request:Request): return {'updated':update_restaurant(id,body.model_dump(),request)}
@app.delete('/api/v1/restaurants/{id}')
def delete_restaurant_api(id:int,request:Request): return delete_restaurant(id,request)
@app.post('/api/v1/slots',status_code=201)
def add_slot_api(body:SlotInput,request:Request): return {'created':add_slot(body.model_dump(),request)}
@app.get('/api/v1/bookings')
def my_bookings_api(request:Request): return my_bookings(request)
@app.delete('/api/v1/bookings/{id}')
def cancel_booking_api(id:int,request:Request): return cancel_booking(id,request)

@app.get('/docs',include_in_schema=False)
def docs(): return get_swagger_ui_html(openapi_url='/openapi.json',title='DineIndia REST API',swagger_ui_parameters={'persistAuthorization':True,'docExpansion':'list'})
@app.get('/openapi.json',include_in_schema=False)
def spec(): return yaml.safe_load((ROOT/'docs/openapi.yaml').read_text())
@app.get('/',response_class=HTMLResponse,include_in_schema=False)
def home(): return (ROOT/'src/index.html').read_text()
@app.get('/graphql',response_class=HTMLResponse,include_in_schema=False)
def ide(): return '''<!doctype html><html><head><link rel="stylesheet" href="https://unpkg.com/graphiql@3/graphiql.min.css"></head><body style="margin:0"><script type="text/plain" id="examples"># Run one operation at a time using the operation selector.
# For protected operations set Headers: {"Authorization":"Bearer YOUR_TOKEN"}
query Restaurants { restaurants(first: 10) { edges { cursor node { id name city price location { coordinates } } } pageInfo { hasNextPage endCursor } } }
query Calendar { calendar(restaurant_id: 1) { id starts_at ends_at available } }
mutation AdminLogin { login(username: "admin", password: "admin-password") { token token_type expires_in } }
mutation CustomerLogin { login(username: "demo", password: "demo-password") { token } }
mutation AddRestaurant { addRestaurant(input:{name:"Demo Kitchen",city:"Chennai",category:"South Indian",price:500,latitude:13.08,longitude:80.27}) { created { id name } } }
mutation UpdateRestaurant($id:Int!) { updateRestaurant(id:$id,input:{price:600}) { updated { id price } } }
mutation AddSlot($id:Int!,$start:String!,$end:String!) { addSlot(input:{restaurant_id:$id,starts_at:$start,ends_at:$end}) { created { id available } } }
mutation Book($slot:ID!) { book(slot_id:$slot) { created { id slot_id } } }
query MyBookings { bookings { id slot_id username } }
mutation Cancel($id:Int!) { cancelBooking(id:$id) { id } }
mutation DeleteRestaurant($id:Int!) { deleteRestaurant(id:$id) { id } }
</script><div id="root" style="height:100vh"></div><script crossorigin src="https://unpkg.com/react@18/umd/react.production.min.js"></script><script crossorigin src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script><script src="https://unpkg.com/graphiql@3/graphiql.min.js"></script><script>ReactDOM.createRoot(document.getElementById('root')).render(React.createElement(GraphiQL,{fetcher:GraphiQL.createFetcher({url:'/graphql'}),defaultQuery:document.getElementById('examples').textContent,defaultHeaders:'{}'}));</script></body></html>'''
schema=build_schema((ROOT/'docs/schema.graphql').read_text())
def wrap(fn):
    def resolve(obj,info,**kw):
        try: return fn(info,**kw)
        except Domain as e: raise GraphQLError(e.code,extensions={'code':e.code,'http':{'status':ERRORS[e.code]}})
    return resolve
schema.query_type.fields['restaurants'].resolve=wrap(lambda info,first=10,**kw:listing(limit=first,**kw))
schema.query_type.fields['map'].resolve=wrap(lambda info,**kw:map_search(**kw))
schema.query_type.fields['calendar'].resolve=wrap(lambda info,**kw:calendar(**kw))
schema.mutation_type.fields['login'].resolve=wrap(lambda info,username,password:issue_token(username,password))
schema.mutation_type.fields['book'].resolve=wrap(lambda info,slot_id: {'created':book(parse_slot_id(slot_id),info.context['request'])})
schema.query_type.fields['restaurant'].resolve=wrap(lambda info,id:get_restaurant(id))
schema.query_type.fields['bookings'].resolve=wrap(lambda info:my_bookings(info.context['request']))
schema.mutation_type.fields['addRestaurant'].resolve=wrap(lambda info,input:{'created':add_restaurant(input,info.context['request'])})
schema.mutation_type.fields['updateRestaurant'].resolve=wrap(lambda info,id,input:{'updated':update_restaurant(id,input,info.context['request'])})
schema.mutation_type.fields['deleteRestaurant'].resolve=wrap(lambda info,id:delete_restaurant(id,info.context['request']))
schema.mutation_type.fields['addSlot'].resolve=wrap(lambda info,input:{'created':add_slot(input,info.context['request'])})
schema.mutation_type.fields['cancelBooking'].resolve=wrap(lambda info,id:cancel_booking(id,info.context['request']))
schema.get_type('Restaurant').fields['slots'].resolve=lambda obj,info: info.context['slots'].get(obj['id'],[])
@app.post('/graphql',include_in_schema=False)
async def graph(request:Request):
    try:
        body=await request.json()
        if not isinstance(body,dict) or not isinstance(body.get('query'),str): raise ValueError()
    except Exception:
        return JSONResponse({'errors':[{'message':'Invalid request','extensions':{'code':'BAD_INPUT','http':{'status':400}}}]},status_code=400)
    grouped=defaultdict(list)
    for s in calendar(): grouped[s['restaurant_id']].append(s)
    result=graphql_sync(schema,body.get('query',''),variable_values=body.get('variables'),operation_name=body.get('operationName'),context_value={'request':request,'slots':grouped})
    out=result.formatted
    for e, original in zip(out.get('errors',[]),result.errors or []):
        ext=e.setdefault('extensions',{}); code=ext.setdefault('code','BAD_INPUT' if original.path is None else 'INTERNAL_ERROR'); ext.setdefault('http',{'status':ERRORS[code]})
    return out
