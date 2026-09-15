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
        for name,kind in [('party_one','WARRIOR'),('party_two','MAGE')]:
            c=Client(base);c.token();c.request('/api/register',dict(username=name,password=password),expect=201);c.login(name,password);c.request('/api/character',dict(name=name,playerClass=kind));clients.append(c)
        a,b=clients
        party=a.request('/api/coop/create',{})
        a.request('/api/coop/create',{},expect=400)
        a.request('/api/command',dict(version=0,action='rest',value=''),expect=400)
        def move(c,party,action,value='',expect=200):return c.request('/api/coop/move',dict(id=party['id'],round=party['round'],action=action,value=value),expect=expect)
        b.request('/api/coop/move',dict(id=party['id'],round=0,action='start',value=''),expect=400)
        party=b.request('/api/coop/join',dict(code=party['id']))
        move(b,party,'start',expect=400)
        party=move(a,party,'start')
        party=move(a,party,'choose','ATTACK')
        move(a,party,'choose','ATTACK',expect=400)
        move(a,party,'cover',expect=400)
        saved=b.request('/api/coop')
        p.terminate();p.wait(timeout=15);p=start()
        a,b=Client(base),Client(base);a.login('party_one',password);b.login('party_two',password)
        restored=b.request('/api/coop');assert restored['round']==saved['round'] and restored['members'][0]['ready'],'Pending turn must survive restart'
        party=move(b,restored,'choose','ability:fireball')
        from concurrent.futures import ThreadPoolExecutor
        for _ in range(35):
            if party['state']!='BATTLE':break
            pa,pb=a.request('/api/coop'),b.request('/api/coop')
            def choose(view):
                me=next(m for m in view['members'] if m['you']);partner=next(m for m in view['members'] if not m['you'])
                if 'Heavy' in view['enemy']['intent']:return 'DEFEND'
                if me['health']<me['maxHealth']*.4 and me['potions']:return 'POTION'
                if partner['health']<partner['maxHealth']*.5 and me['resource']>=12:return 'MEND'
                ability=next((x for x in me['abilities'] if x['id'] in ['fireball','ko_slash'] and x['cooldown']==0 and me['resource']>=x['cost']),None)
                return 'ability:'+ability['id'] if ability else 'ATTACK'
            with ThreadPoolExecutor(max_workers=2) as pool:
                results=list(pool.map(lambda pair:move(pair[0],pair[1],'choose',choose(pair[1])),[(a,pa),(b,pb)]))
            party=a.request('/api/coop')
        assert party['state']=='VICTORY',party
        saves=[c.request('/api/export') for c in [a,b]]
        assert all(s['kills']==1 and len(s['inventory'])==1 and s['player']['coins']>=40 for s in saves)
        move(a,party,'choose','ATTACK',expect=400)
        move(a,party,'start',expect=400)
        assert saves==[c.request('/api/export') for c in [a,b]],'Completed run must not reward twice'
        move(a,party,'leave');move(b,party,'leave')
        state=a.request('/api/game');a.request('/api/command',dict(version=state['version'],action='rest',value=''))
        print('Co-op check passed: two accounts, simultaneous moves, solo lock, pending-round restart, victory, one reward each, duplicate rejection, return to solo.')
    finally:p.terminate();p.wait(timeout=15)
