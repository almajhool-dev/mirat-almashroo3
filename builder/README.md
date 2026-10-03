# Binaa AI — AI App Builder

واجهة عربية RTL لمنصة بناء التطبيقات بالذكاء الاصطناعي، مضافة كمجلد مستقل داخل المستودع.

## الحالة الحالية
- Dashboard + Projects
- إنشاء مشروع من Prompt
- قوالب جاهزة
- Studio بثلاث لوحات: Chat / Preview / Files
- Demo AI mode يعمل بدون API key
- حفظ المشاريع عبر localStorage
- Responsive للموبايل
- لا توجد مفاتيح سرية داخل المتصفح

## فتحها
بعد نشر المستودع عبر Vercel أو GitHub Pages:
`/builder/`

## الانتقال للإنتاج
1. إنشاء backend حقيقي لمشاريع المستخدمين.
2. ربط مزود LLM من الخادم فقط عبر environment variables.
3. قاعدة بيانات للمشاريع والنسخ.
4. Sandbox آمن لبناء/اختبار التطبيقات وعدم تنفيذ كود المستخدم على نفس الخادم.
5. GitHub OAuth وعمليات commits/branches.
6. Vercel deployments لكل مشروع.

هذه النسخة متعمدة أن تكون Demo آمنة: لا تنفذ كوداً مولداً من المستخدم على الخادم.
