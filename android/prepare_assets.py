from pathlib import Path
import shutil
root=Path(__file__).resolve().parent.parent
out=root/'android/app/src/main/assets'
out.mkdir(parents=True,exist_ok=True)
s=(root/'index.html').read_text()
s=s.replace('Version 3.12','Android 1.0.0 · Basis 3.12')
s=s.replace('Die Daten werden nur in diesem Browser auf diesem Gerät gespeichert.','Die Daten werden nur in dieser App auf diesem Gerät gespeichert.')
s=s.replace("if('serviceWorker'in navigator&&location.protocol.startsWith('http'))", "if(!window.MalteserNative&&'serviceWorker'in navigator&&location.protocol.startsWith('http'))")
s=s.replace("$('confirmPrint').onclick=()=>{save();renderPaper();window.print()}", "$('confirmPrint').onclick=()=>{save();renderPaper();if(window.MalteserNative){MalteserNative.printDocument($('previewFrame').srcdoc)}else window.print()}")
s=s.replace("$('export').onclick=()=>{save();const a=", "$('export').onclick=()=>{save();if(window.MalteserNative){MalteserNative.exportBackup(JSON.stringify(state,null,2),`dienstzettel-${$('month').value}.json`);return}const a=")
s=s.replace("if(!FieldSpeech){status.textContent=", "if(window.MalteserNative){MalteserNative.speak(id);return}if(!FieldSpeech){status.textContent=")
s=s.replace("document.querySelectorAll('[data-voice-field]').forEach", "window.malteserSpeechResult=(id,spoken)=>{if(!['date','endDate','start','end'].includes(id))return;const value=id==='date'||id==='endDate'?fieldDate(spoken):fieldTime(spoken);if(value){$(id).value=value;$(id).dispatchEvent(new Event('change',{bubbles:true}));$('fieldVoiceStatus').textContent='✓ '+spoken+' → '+value}else $('fieldVoiceStatus').textContent='Nicht erkannt: '+spoken+'. Bitte erneut sprechen oder eintippen.'};\ndocument.querySelectorAll('[data-voice-field]').forEach")
s=s.replace('Unterschrift dauerhaft in diesem Browser gespeichert.', 'Unterschrift dauerhaft in dieser App gespeichert.')
(out/'index.html').write_text(s)
for name in ['icon-192.png','icon-512.png','manifest.webmanifest']: shutil.copyfile(root/name,out/name)

shutil.copyfile(root/"icon-192.png",root/"android/app/src/main/res/drawable/icon.png")
