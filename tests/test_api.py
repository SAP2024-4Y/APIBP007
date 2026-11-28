import os
import tempfile
os.environ['DB_PATH']=tempfile.mktemp(suffix='.db')
from fastapi.testclient import TestClient
from src.main import app
client=TestClient(app)
def test_list_map_calendar():
    assert client.get('/health').json()=={'status':'ok'}
    a=client.get('/api/v1/restaurants?limit=3').json()
    b=client.get('/api/v1/restaurants',params={'limit':3,'after':a['after']}).json()
    assert a['has_more'] and a['items'][-1]['id']<b['items'][0]['id']
    assert len(client.get('/api/v1/restaurants/map?latitude=13.0827&longitude=80.2707&radius_km=10').json())==10
    assert client.get('/api/v1/calendar').json()
def test_booking_parity():
    slot=client.get('/api/v1/calendar').json()[0]['id']
    body={'slot_id':slot}
    assert client.post('/api/v1/bookings',json=body).status_code==401
    t=client.post('/api/v1/auth/token',json={'username':'demo','password':'demo-password'}).json()['token']
    h={'Authorization':'Bearer '+t}
    assert client.post('/api/v1/bookings',json=body,headers=h).status_code==201
    assert client.post('/api/v1/bookings',json=body,headers=h).status_code==409
    g=client.post('/graphql',json={'query':'mutation { book(slot_id: "%s") { created { id } } }'%slot},headers=h).json()
    assert g['errors'][0]['extensions']['code']=='CONFLICT'
def test_graph_list():
    g=client.post('/graphql',json={'query':'{ restaurants(first: 2) { edges { node { name slots { id } } } pageInfo { hasNextPage } } }'}).json()
    assert g['data']['restaurants']['pageInfo']['hasNextPage']

def test_admin_and_city():
    def headers(user):
        t=client.post('/api/v1/auth/token',json={'username':user,'password':user+'-password'}).json()['token']
        return {'Authorization':'Bearer '+t}
    a=headers('admin'); c=headers('demo')
    body={'name':'Test Kitchen','city':'Chennai','category':'South Indian','price':450,'latitude':13.08,'longitude':80.27}
    assert client.post('/api/v1/restaurants',json=body,headers=c).status_code==403
    r=client.post('/api/v1/restaurants',json=body,headers=a); assert r.status_code==201
    i=r.json()['created']['id']
    assert all(x['city']=='Chennai' for x in client.get('/api/v1/restaurants?city=Chennai').json()['items'])
    assert client.patch(f'/api/v1/restaurants/{i}',json={'price':500},headers=a).status_code==200
    assert client.delete(f'/api/v1/restaurants/{i}',headers=a).status_code==200


def test_fifty_cursor_pages():
    ids=[]; after=None
    for page in range(5):
        params={'limit':10}
        if after: params['after']=after
        result=client.get('/api/v1/restaurants',params=params).json()
        assert len(result['items'])==10
        ids.extend(x['id'] for x in result['items'])
        assert result['has_more']==(page<4)
        after=result['after']
    assert len(set(ids))==50


def test_graph_login_and_error_status():
    g=client.post('/graphql',json={'query':'mutation { login(username:"admin",password:"admin-password") { token token_type expires_in } }'}).json()
    assert g['data']['login']['token_type']=='Bearer'
    assert g['data']['login']['expires_in']==3600
    g=client.post('/graphql',json={'query':'mutation { login(username:"demo",password:"wrong") { token } }'}).json()
    assert g['errors'][0]['extensions']=={'code':'UNAUTHENTICATED','http':{'status':401}}


def test_internal_error_fault_injection(monkeypatch):
    import src.main as main
    def fail(*args,**kwargs): raise RuntimeError('test-only fault')
    monkeypatch.setattr(main,'listing',fail)
    safe=TestClient(app,raise_server_exceptions=False)
    assert safe.get('/api/v1/restaurants').status_code==500
    g=safe.post('/graphql',json={'query':'{ restaurants { edges { cursor } } }'}).json()
    assert g['errors'][0]['extensions']['code']=='INTERNAL_ERROR'
