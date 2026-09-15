"""One isolated two-player run, with reconnect and duplicate-reward checks."""
from pathlib import Path
exec(Path(__file__).with_name('web_smoke.py').read_text().split('with tempfile.TemporaryDirectory')[0])
with tempfile.TemporaryDirectory(prefix='wayfarer-coop-') as tmp:
    with socket.socket() as s:s.bind(('127.0.0.1',0));port=s.getsockname()[1]
    base=f'http://127.0.0.1:{port}'
    def start():
        log=open(Path(tmp)/'server.log','a')
        p=subprocess.Popen(['java','-jar',str(jar),f'--server.port={port}'],cwd=tmp,stdout=log,stderr=subprocess.STDOUT)
        for _ in range(100):
            try:
                if urllib.request.urlopen(base+'/health',timeout=1).status==200:return p
            except Exception:time.sleep(.1)
        p.terminate();raise AssertionError(Path(tmp,'server.log').read_text())
    p=start()
    try:
        password=secrets.token_urlsafe(20)
        clients=[]
        for name in ['story_one','story_two']:
            c=Client(base);c.token();c.request('/api/register',dict(username=name,password=password),expect=201);c.login(name,password);c.request('/api/character',dict(name=name,playerClass='CLERIC'));clients.append(c)
        a,b=clients
        a.request('/api/coop/create',dict(route=2),expect=400)
        party=a.request('/api/coop/create',dict(route=0));party=b.request('/api/coop/join',dict(code=party['id']))
        def move(c,view,action,value='',expect=200):return c.request('/api/coop/move',dict(id=view['id'],round=view['round'],action=action,value=value),expect=expect)
        party=move(a,party,'start');party=move(a,party,'choose','OBJECTIVE')
        p.terminate();p.wait(timeout=15);p=start()
        a,b=Client(base),Client(base);a.login('story_one',password);b.login('story_two',password)
        party=b.request('/api/coop');assert party['members'][0]['ready'] and party['story']
        party=move(b,party,'choose','ATTACK')
        for turn in range(110):
            if party['state']!='BATTLE':break
            choices=[]
            for idx,m in enumerate(party['members']):
                heal=next(x for x in m['abilities'] if x['id']=='prayer')
                if m['health']<m['maxHealth']*.6 and m['resource']>=heal['cost'] and heal['cooldown']==0:choice='ability:prayer'
                elif m['health']<m['maxHealth']*.4 and m['potions']:choice='POTION'
                elif (party['objective']['progress']<3 or party['objective']['integrity']<=60) and idx==turn%2:choice='OBJECTIVE'
                elif 'Heavy' in party['enemy']['intent'] and m['name'] in party['enemy']['intent']:choice='DEFEND'
                else:choice='ATTACK'
                choices.append(choice)
            move(a,party,'choose',choices[0]);party=move(b,party,'choose',choices[1])
        assert party['state']=='VICTORY',party
        saves=[c.request('/api/export') for c in [a,b]]
        assert all('route:clear:0' in s['claimed'] for s in saves)
        assert all(len(s['inventory'])==1 for s in saves)
        move(a,party,'choose','ATTACK',expect=400)
        assert saves==[c.request('/api/export') for c in [a,b]]
        move(a,party,'leave');move(b,party,'leave')
        print('Shared-story HTTP check passed: two accounts, blocked route skip, pending move restart, three stages, objective work, individual progression, duplicate reward rejection.')
    finally:p.terminate();p.wait(timeout=15)
