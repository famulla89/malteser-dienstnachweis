from pathlib import Path
p=Path("android-app/app/src/main/assets/index.html")
s=p.read_text(encoding="utf-8")
changes=[["window.print()","DienstNative.print()"],["const a=document.createElement('a');a.href=URL.createObjectURL(new Blob([JSON.stringify(state,null,2)],{type:'application/json'}));a.download=`dienstzettel-${$('month').value}.json`;a.click();setTimeout(()=>URL.revokeObjectURL(a.href),1000)","DienstNative.saveJson('dienstzettel-'+$('month').value+'.json',JSON.stringify(state,null,2))"],["if(!FieldSpeech){","if(window.DienstNative){DienstNative.speech(id);return}if(!FieldSpeech){"],["const FieldSpeech=","window.nativeSpeechResult=(id,spoken)=>{const value=id==='date'||id==='endDate'?fieldDate(spoken):fieldTime(spoken);if(value&&$(id)){$(id).value=value;$(id).dispatchEvent(new Event('change',{bubbles:true}));$('fieldVoiceStatus').textContent=value}else $('fieldVoiceStatus').textContent='Bitte Datum oder Uhrzeit erneut sprechen.'};const FieldSpeech="],["if('serviceWorker'in navigator&&location.protocol.startsWith('http'))navigator.serviceWorker.register('./sw.js').catch(()=>{});",""]]
for old,new in changes:
    if old not in s: raise RuntimeError("Source pattern missing: "+old[:60])
    s=s.replace(old,new)
p.write_text(s,encoding="utf-8")

s=p.read_text(encoding="utf-8")
css=Path("android-app/modern.css").read_text(encoding="utf-8")
js=Path("android-app/modern.js").read_text(encoding="utf-8")
s=s.replace("</head>","<style>"+css+"</style></head>",1)
a,b=s.rsplit("</body>",1)
s=a+"<script>"+js+"</script></body>"+b
p.write_text(s,encoding="utf-8")

s=s.replace("dauerhaft in diesem Browser gespeichert","dauerhaft in dieser App gespeichert").replace("Die Daten werden nur in diesem Browser auf diesem Gerät gespeichert.","Die Daten werden nur in dieser App auf diesem Gerät gespeichert.")
p.write_text(s,encoding="utf-8")
