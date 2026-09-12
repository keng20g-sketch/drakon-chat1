package org.drakon.drakonchat;

import android.app.*;import android.os.*;import android.content.*;import android.graphics.Color;import android.graphics.Typeface;import android.view.*;import android.view.Window;import android.view.WindowInsets;import android.widget.*;import org.json.*;import java.nio.charset.StandardCharsets;import java.security.*;import java.security.spec.*;import java.util.*;import javax.crypto.SecretKeyFactory;import javax.crypto.spec.PBEKeySpec;

public class MainActivity extends Activity {
    LinearLayout root; String email="", role=""; JSONObject users; final int BG=Color.rgb(9,9,12), CARD=Color.rgb(25,27,34), TXT=Color.rgb(242,242,247), MUTED=Color.rgb(185,185,195); android.content.SharedPreferences prefs;
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        // Screenshots/screen recording are allowed (do not use FLAG_SECURE).
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        prefs=getSharedPreferences("db",0); users=load(); ensureAdmin(); showLogin();
    }
    TextView tv(String s,int sp){ TextView t=new TextView(this); t.setText(s);t.setTextColor(TXT);t.setTextSize(sp);t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);t.setPadding(18,12,18,12);return t; }
    EditText input(String hint,boolean pass){ EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(Color.GRAY);e.setTextColor(TXT);e.setTextSize(16);e.setSingleLine(true);e.setPadding(dp(18),dp(4),dp(18),dp(4)); if(pass)e.setInputType(0x81); else e.setInputType(0x21); return e; }
    Button btn(String text){ Button b=new Button(this);b.setText(text);b.setTextColor(TXT);b.setTextSize(15);b.setAllCaps(false);b.setBackgroundColor(CARD);b.setMinHeight(dp(58)); return b; }
    void base(){
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        root.setPadding(dp(22),dp(18),dp(22),dp(18));
        root.setMinimumHeight(getResources().getDisplayMetrics().heightPixels);
        root.setBackgroundColor(BG);
        scroll.addView(root,new ScrollView.LayoutParams(-1,-1));
        setContentView(scroll);
    }
    void add(View v,int h){root.addView(v,new LinearLayout.LayoutParams(-1,dp(h)));}
    void showLogin(){base(); Space sp=new Space(this);add(sp,25); ImageView im=new ImageView(this);im.setImageResource(org.drakon.drakonchat.R.drawable.deewan);im.setScaleType(ImageView.ScaleType.CENTER_INSIDE);add(im,180); TextView title=tv("Drakon Chat",25);title.setGravity(Gravity.CENTER);title.setTypeface(null,Typeface.BOLD);add(title,60); TextView sub=tv("تسجيل الدخول",16);add(sub,45); EditText e=input("البريد الإلكتروني",false),p=input("كلمة السر",true);add(e,58);add(p,58); Button login=btn("تسجيل الدخول");add(login,62);login.setOnClickListener(v->login(e.getText().toString().trim(),p.getText().toString()));Button reg=btn("إنشاء حساب عضو");add(reg,62);reg.setOnClickListener(v->showRegister()); }
    void showRegister(){base();add(tv("إنشاء حساب عضو",24),65);EditText e=input("البريد الإلكتروني",false),p=input("كلمة السر - 6 أحرف على الأقل",true),c=input("تأكيد كلمة السر",true);add(e,58);add(p,58);add(c,58);Button b=btn("إنشاء الحساب");add(b,62);b.setOnClickListener(v->{String em=e.getText().toString().trim(),pw=p.getText().toString(),cf=c.getText().toString();if(!em.contains("@")||pw.length()<6||!pw.equals(cf)){notice("تحقق من البيانات وكلمة السر.");return;}try{if(users.has(em)){notice("الحساب موجود مسبقاً.");return;}String[] h=hash(pw,null);JSONObject u=new JSONObject();u.put("email",em);u.put("password_salt",h[0]);u.put("password_hash",h[1]);u.put("role","member");u.put("coins",0);u.put("active",true);users.put(em,u);save();notice("تم إنشاء الحساب.");showLogin();}catch(Exception x){notice("حدث خطأ.");}});Button back=btn("رجوع");add(back,62);back.setOnClickListener(v->showLogin());}
    void login(String em,String pw){try{if(!users.has(em)){notice("الحساب غير موجود.");return;}JSONObject u=users.getJSONObject(em);if(!u.optBoolean("active",true)){notice("الحساب غير نشط.");return;}if(!verify(pw,u.getString("password_salt"),u.getString("password_hash"))){notice("البريد أو كلمة السر غير صحيحة.");return;}email=em;role=u.optString("role","member");showHome();}catch(Exception x){notice("حدث خطأ.");}}
    void showHome(){base();add(tv("مرحباً بك في Drakon Chat",23),70);try{JSONObject u=users.getJSONObject(email);add(tv("رصيد العملات: "+u.optInt("coins",0),18),55);}catch(Exception ignored){}; String[][] pages={{"الرسائل","هنا تضاف المحادثات وربط السيرفر."},{"الأصدقاء","هنا تضاف الأصدقاء."},{"الهدايا","هنا تضاف الهدايا."},{"VIP","هنا تضاف باقات VIP."},{"الملف الشخصي","هنا تظهر بيانات الحساب."}};for(String[] p:pages){Button b=btn(p[0]);add(b,58);b.setOnClickListener(v->simple(p[0],p[1]));}if("admin".equals(role)){Button a=btn("لوحة الإدارة");add(a,62);a.setOnClickListener(v->showAdmin());}Button out=btn("تسجيل الخروج");add(out,62);out.setOnClickListener(v->{email="";role="";showLogin();});}
    void simple(String title,String body){base();add(tv(title,24),70);add(tv(body,17),120);Button b=btn("رجوع");add(b,62);b.setOnClickListener(v->showHome());}
    void showAdmin(){base();add(tv("لوحة الإدارة",24),65);try{add(tv("رصيد المدير: "+users.getJSONObject(email).optInt("coins",0),17),50);}catch(Exception ignored){}Button self=btn("إضافة 100 عملة للمدير");add(self,62);self.setOnClickListener(v->{try{JSONObject u=users.getJSONObject(email);u.put("coins",u.optInt("coins",0)+100);save();showAdmin();}catch(Exception ignored){}});add(tv("الأعضاء",18),50);try{Iterator<String> it=users.keys();while(it.hasNext()){String em=it.next();JSONObject u=users.getJSONObject(em);if("member".equals(u.optString("role"))){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);TextView t=tv(em+" | العملات: "+u.optInt("coins",0),15);row.addView(t,new LinearLayout.LayoutParams(0,60,1));Button g=btn("إعطاء");row.addView(g,new LinearLayout.LayoutParams(100,60));g.setOnClickListener(v->giveDialog(em));add(row,62);}}}catch(Exception ignored){}Button back=btn("رجوع");add(back,62);back.setOnClickListener(v->showHome());}
    void giveDialog(String target){ final EditText amount=input("عدد العملات",false); new AlertDialog.Builder(this).setTitle("إعطاء عملات").setView(amount).setPositiveButton("إعطاء",(d,w)->{try{int n=Integer.parseInt(amount.getText().toString());if(n<=0||n>1000000){notice("عدد العملات غير صالح.");return;}JSONObject u=users.getJSONObject(target);u.put("coins",u.optInt("coins",0)+n);save();notice("تم إعطاء "+n+" عملة للعضو.");showAdmin();}catch(Exception x){notice("اكتب عدداً صحيحاً.");}}).setNegativeButton("إلغاء",null).show();}
    void notice(String s){new AlertDialog.Builder(this).setTitle("Drakon Chat").setMessage(s).setPositiveButton("حسناً",null).show();}
    JSONObject load(){try{return new JSONObject(prefs.getString("users","{}"));}catch(Exception e){return new JSONObject();}}
    void save(){prefs.edit().putString("users",users.toString()).apply();}
    void ensureAdmin(){try{if(!users.has("admin@drakon.com")){String[] h=hash("Admin123",null);JSONObject u=new JSONObject();u.put("email","admin@drakon.com");u.put("password_salt",h[0]);u.put("password_hash",h[1]);u.put("role","admin");u.put("coins",0);u.put("active",true);users.put("admin@drakon.com",u);save();}}catch(Exception ignored){}}
    String[] hash(String password,String salt) throws Exception {if(salt==null){byte[] b=new byte[16];new SecureRandom().nextBytes(b);salt=Base64.getEncoder().encodeToString(b);}PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt.getBytes(StandardCharsets.UTF_8),120000,256);byte[] d=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();return new String[]{salt,Base64.getEncoder().encodeToString(d)};}
    boolean verify(String p,String s,String d)throws Exception{return MessageDigest.isEqual(hash(p,s)[1].getBytes(StandardCharsets.UTF_8),d.getBytes(StandardCharsets.UTF_8));}
}
