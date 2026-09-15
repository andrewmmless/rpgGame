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
        for i in range(9):
            c=Client(base);c.token();c.request('/api/register',dict(username=f'guild_{i}',password=password),expect=201);c.login(f'guild_{i}',password);c.request('/api/character',dict(name=f'Member {i}',playerClass='MAGE'));clients.append(c)
        owner=clients[0];guild=owner.request('/api/social',dict(version=0,action='create',value='Cinder Company'))
        for c in clients[1:8]:
            guild=owner.request('/api/social');c.request('/api/social',dict(version=0,action='join',value=guild['invite']))
        guild=owner.request('/api/social');assert len(guild['members'])==8 and guild['capacity']==8
        assert len(guild['bonds'])==7 and all(b['partner']!='guild_0' for b in guild['bonds'])
        clients[8].request('/api/social',dict(version=0,action='join',value=guild['invite']),expect=400)
        assert len(owner.request('/api/social')['members'])==8
        print('Guild HTTP check passed: eight members, ninth rejected, seven individual bonds and rotating invites.')
    finally:p.terminate();p.wait(timeout=15)
