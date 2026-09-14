"""HTTP checks against an isolated server/database; never touches real player saves."""
import http.cookiejar,json,urllib.request,urllib.error,urllib.parse,tempfile,subprocess,time,socket,secrets
from pathlib import Path
jar=Path(__file__).resolve().parents[1]/'target/rpg-game-4.1.0.jar'
class Client:
    def __init__(self,base):
        self.base=base;self.opener=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()));self.csrf=None
    def request(self,path,body=None,form=False,expect=200,csrf=True):
        headers={};encoded=None
        if body is not None:
            headers['Content-Type']='application/x-www-form-urlencoded' if form else 'application/json'
            if csrf and self.csrf:headers[self.csrf['headerName']]=self.csrf['token']
            encoded=(urllib.parse.urlencode(body) if form else json.dumps(body)).encode()
        try:r=self.opener.open(urllib.request.Request(self.base+path,data=encoded,headers=headers),timeout=10)
        except urllib.error.HTTPError as e:r=e
        raw=r.read().decode();assert r.status==expect,(path,r.status,raw[:400])
        try:return json.loads(raw)
        except:return raw
    def token(self):self.csrf=self.request('/api/csrf')
    def login(self,user,password):self.token();self.request('/api/login',dict(username=user,password=password),form=True,expect=204);self.token()
with tempfile.TemporaryDirectory(prefix='hearthglen-http-') as tmp:
    with socket.socket() as s:s.bind(('127.0.0.1',0));port=s.getsockname()[1]
    base=f'http://127.0.0.1:{port}'
    def start():
        log=open(Path(tmp)/'server.log','a')
        proc=subprocess.Popen(['java','-jar',str(jar),f'--server.port={port}'],cwd=tmp,stdout=log,stderr=subprocess.STDOUT)
        for _ in range(100):
            try:
                if urllib.request.urlopen(base+'/health',timeout=1).status==200:return proc
            except Exception:time.sleep(.1)
        proc.terminate();raise AssertionError(Path(tmp,'server.log').read_text())
    proc=start()
    try:
        c=Client(base);c.token()
        assert c.request('/api/me')=={'signedIn':False}
        c.request('/api/game',expect=401)
        c.request('/api/register',dict(username='qa_one',password='irrelevant'),expect=401,csrf=False)
        password=secrets.token_urlsafe(24)
        c.request('/api/register',dict(username='qa_one',password=password),expect=201)
        c.login('qa_one',password)
        assert c.request('/api/game')['needsCharacter']
        state=c.request('/api/character',dict(name='Test Mage',playerClass='MAGE'))
        assert state['mode']=='TOWN'
        def command(action,value=''):
            global state
            state=c.request('/api/command',dict(version=state['version'],action=action,value=value));return state
        c.request('/api/command',dict(version=99,action='rest',value=''),expect=409)
        assert c.request('/api/game')['version']==0
        command('adventure','WHISPERING_WOODS');command('continue');command('combat','ability:fireball')
        saved=c.request('/api/export');assert saved['battle']['cooldowns']['fireball']==2
        assert c.request('/api/game')['player']['health']==state['player']['health']
        c.request('/api/command',dict(version=state['version'],action='rest',value=''),expect=400)
        assert c.request('/api/game')['version']==state['version']
        other=Client(base);other.token();other.request('/api/register',dict(username='qa_two',password=password),expect=201);other.login('qa_two',password)
        assert other.request('/api/game')['needsCharacter']
        other.request('/api/export',expect=400)
        other.request('/api/import',dict(text='Name=Legacy\nClass=Cleric\nLevel=2\nHealth=8\nMaxHealth=30\nXp=10\nXpToNextLevel=35\nCoins=5\nPotions=2\n'))
        assert other.request('/api/game')['player']['type']=='CLERIC'
        board=c.request('/api/leaderboard');assert len(board)==2 and board[0]['ranked'] and not board[1]['ranked']
        assert all('username' not in row for row in board)
        proc.terminate();proc.wait(timeout=15);proc=start()
        c=Client(base);c.login('qa_one',password)
        assert c.request('/api/export')==saved,'Restart must preserve encounter, health, resource, statuses and cooldowns'
        state=c.request('/api/game');command('combat','ATTACK')
        assert state['mode']=='TRAIL' and state['kills']==1
        c.request('/api/logout',{},expect=204);c.request('/api/game',expect=401)
        print('HTTP checks passed: registration, login, CSRF, account isolation, stale-tab protection, console import, battle autosave, server restart, victory, logout.')
    finally:
        proc.terminate();proc.wait(timeout=15)
