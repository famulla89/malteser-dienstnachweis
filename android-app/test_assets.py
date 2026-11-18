from pathlib import Path
import re,subprocess
s=Path("android-app/app/src/main/assets/index.html").read_text(encoding="utf-8")
assert not re.search(r'hourlyRate|dashPay|dienst-rate|Stundenlohn|brutto',s,re.I)
scripts=re.findall(r'<script>(.*?)</script>',s,re.S)
assert len(scripts)==2, len(scripts)
for i,js in enumerate(scripts):
    p=Path('/tmp')/f'dienst-test-{i}.js';p.write_text(js,encoding='utf-8');subprocess.run(['node','--check',str(p)],check=True)
f=re.search(r'function hours\(.*?(?=function normalizedTime)',scripts[0],re.S).group()
subprocess.run(['node','-e',f+"""\nconst a=require('node:assert/strict'); for(const [t,s,e,d,h] of [['shgdb','18:00','18:00','2026-11-28',3],['shgdb','18:00','18:00','2026-11-29',6],['shgde','10:00','11:00','2026-11-27',2],['thgde','10:00','11:00','2026-11-27',1],['training','10:00','11:15','2026-11-27',1.25],['ma','10:00','10:30','2026-11-27',0.5],['admin','23:30','00:30','2026-11-28',1]])a.equal(hours(t,'2026-11-27',s,d,e),h);a(Number.isNaN(hours('admin','2026-11-27','11:00','2026-11-27','10:00')));console.log('8 calculation checks passed');"""],check=True)
