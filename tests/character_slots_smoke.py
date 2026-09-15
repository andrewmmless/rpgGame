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
        c=Client(base);c.token();c.request('/api/register',dict(username='slots_test',password=password),expect=201);c.login('slots_test',password)
        c.request('/api/character',dict(name='Original',playerClass='MAGE'))
        original=c.request('/api/export')
        assert len(c.request('/api/characters')['slots'])==4
        state=c.request('/api/characters/select',dict(slot=1,generation=0));assert state['needsCharacter'] and state['generation']==1
        c.request('/api/command',dict(version=0,action='rest',value='',generation=0),expect=409)
        c.request('/api/character',dict(name='Second',playerClass='WARRIOR',generation=0),expect=409)
        c.request('/api/character',dict(name='Second',playerClass='WARRIOR',generation=1));second=c.request('/api/export')
        c.request('/api/characters/select',dict(slot=0,generation=1));assert c.request('/api/export')==original
        c.request('/api/characters/select',dict(slot=2,generation=2))
        c.request('/api/character',dict(name='Duplicate',playerClass='MAGE',generation=3),expect=400)
        c.request('/api/characters/select',dict(slot=1,generation=3));assert c.request('/api/export')==second
        party=c.request('/api/coop/create',{})
        c.request('/api/characters/select',dict(slot=0,generation=4),expect=400)
        c.request('/api/coop/move',dict(id=party['id'],round=party['round'],action='leave',value=''))
        p.terminate();p.wait(timeout=15);p=start();c=Client(base);c.login('slots_test',password)
        assert c.request('/api/export')==second
        c.request('/api/characters/select',dict(slot=0,generation=4));assert c.request('/api/export')==original
        print('Character slots passed: separate saves, stale request protection, one per class, party lock and restart persistence.')
    finally:p.terminate();p.wait(timeout=15)
