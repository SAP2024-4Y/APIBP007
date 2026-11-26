import json
from urllib.request import urlopen, Request
base='http://localhost:8080'
a=urlopen(base+'/api/v1/restaurants?limit=1').read()
b=urlopen(base+'/api/v1/calendar?restaurant_id=1').read()
q={'query':'{ restaurants(first:1) { edges { node { id name location { coordinates } slots { id } } } } }'}
c=urlopen(Request(base+'/graphql',data=json.dumps(q).encode(),headers={'Content-Type':'application/json'})).read()
print('REST:',len(a)+len(b),'bytes, 2 requests; GraphQL:',len(c),'bytes, 1 request')
