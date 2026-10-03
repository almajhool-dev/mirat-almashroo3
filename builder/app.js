const $=s=>document.querySelector(s),$$=s=>document.querySelectorAll(s);
const KEY='binaa-projects-v1';let projects=JSON.parse(localStorage.getItem(KEY)||'[]');let current=null;
const templates=[['Landing Page','صفحة هبوط تسويقية سريعة','🚀'],['Portfolio','معرض أعمال شخصي أنيق','◈'],['SaaS Dashboard','لوحة تحكم SaaS كاملة','▦'],['Store','متجر إلكتروني للموبايل','🛍']];
function save(){localStorage.setItem(KEY,JSON.stringify(projects));renderProjects()}
function renderProjects(){const g=$('#projectGrid');if(!projects.length){g.innerHTML='<div class="empty">ما عندك مشاريع بعد.<br>اكتب فكرتك بالأعلى وخلّينا نبني أول مشروع.</div>';return}g.innerHTML=projects.map((p,i)=>'<article class="project-card"><span class="pill">PROJECT</span><h4>'+esc(p.name)+'</h4><p>'+esc(p.prompt)+'</p><small class="meta">تم الإنشاء محلياً · '+p.files+' ملفات</small><div class="card-actions"><button onclick="openProject('+i+')">فتح الاستوديو</button><button onclick="deleteProject('+i+')">حذف</button></div></article>').join('')}
function renderTemplates(){const g=$('#templateGrid');g.innerHTML=templates.map(t=>'<article class="template"><div style="font-size:28px">'+t[2]+'</div><h4>'+t[0]+'</h4><p>'+t[1]+'</p><button class="card-actions" onclick="useTemplate('+JSON.stringify(t[0])+')">استخدام القالب</button></article>').join('')}
function esc(s){return String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[c]))}
function show(view){$$('.view').forEach(x=>x.classList.remove('active'));$('#'+view).classList.add('active');$$('.nav').forEach(x=>x.classList.toggle('active',x.dataset.view===view));$('#pageTitle').textContent={projects:'مشاريعي',templates:'القوالب',settings:'الإعدادات'}[view]||'الاستوديو'}
$$('.nav').forEach(b=>b.onclick=()=>show(b.dataset.view));$('#newProject').onclick=()=>{$('#idea').focus();show('projects')};
$$('.suggestions button').forEach(b=>b.onclick=()=>{$('#idea').value=b.dataset.prompt;$('#idea').focus()});
$('#build').onclick=build;$('#idea').addEventListener('keydown',e=>{if(e.key==='Enter')build()});
function build(){const prompt=$('#idea').value.trim();if(!prompt)return $('#idea').focus();const name=prompt.replace(/^ابنِ|^سوي|^اعمل/i,'').trim().slice(0,32)||'مشروع جديد';const p={name,prompt,files:7,created:new Date().toISOString()};projects.unshift(p);save();openProject(0)}
window.openProject=i=>{current=projects[i];$('#studioName').textContent=current.name;$('#studioStatus').textContent='جاهز — Demo Mode';show('studio');$('#messages').innerHTML='';addMsg('ai','هلا بيك 👋\nحللت طلبك مبدئياً. جهزت هيكل مشروع تجريبي ومعاينة حية.\n\nاطلب أي تعديل، مثل: «غيّر الألوان»، «أضف صفحة أسعار»، أو «حوّل التصميم للموبايل».');renderPreview(current.prompt)};
window.deleteProject=i=>{projects.splice(i,1);save()};window.useTemplate=t=>{$('#idea').value='ابنِ '+t+' احترافي ومتجاوب للموبايل';show('projects');$('#idea').focus()};
function addMsg(role,text){const d=document.createElement('div');d.className='msg '+role;d.textContent=text;$('#messages').appendChild(d);$('#messages').scrollTop=99999}
function renderPreview(prompt){$('#preview').innerHTML='<div class="site-preview"><span class="tag">BUILT WITH BINAA AI</span><h2>'+esc(current?.name||'مشروعك الجديد')+'</h2><p>هذه معاينة تفاعلية لمشروع تم توليده من وصفك: <b>'+esc(prompt)+'</b>. عدّل المشروع من المحادثة، وستنعكس التغييرات على المعاينة في النسخة الكاملة.</p><span class="fake-btn">ابدأ الآن</span></div>'}
$('#send').onclick=send;$('#chatInput').addEventListener('keydown',e=>{if(e.key==='Enter'&&!e.shiftKey){e.preventDefault();send()}});
function send(){const v=$('#chatInput').value.trim();if(!v)return;addMsg('user',v);$('#chatInput').value='';setTimeout(()=>{addMsg('ai','تم استلام التعديل في Demo Mode ✓\n\nالتغيير المقترح: '+v+'\n\nفي وضع الإنتاج سيتم تمرير الطلب إلى مزود AI عبر الخادم ثم تحديث ملفات المشروع والمعاينة بأمان.');renderPreview((current?.prompt||'مشروعك')+' — '+v)},350)}
$('#back').onclick=()=>show('projects');$('#clear').onclick=()=>{if(confirm('حذف المشاريع المحلية؟')){projects=[];save()}};$('#publish').onclick=()=>alert('Demo Mode: النشر الحقيقي يحتاج ربط GitHub/Vercel ومفتاح مزود AI. المشروع الحالي محفوظ محلياً.');
$('#theme').onclick=()=>document.body.classList.toggle('light');
$('#language').onchange=e=>{if(e.target.selectedIndex===1)alert('واجهة English ستكون متاحة في الإصدار التالي.')} ;
renderProjects();renderTemplates();
