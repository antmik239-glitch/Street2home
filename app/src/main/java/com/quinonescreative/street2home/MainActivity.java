package com.quinonescreative.street2home;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
  final int BG=0xff0d1218, PANEL=0xff1b2530, WHITE=0xfff4f5f7, ORANGE=0xffff923e;
  LinearLayout root, content, navigation;
  SharedPreferences saved;
  String category="";
  String[][] places={
    {"Handy Foods","Food","Ottawa, Illinois","Ask about current food assistance availability."},
    {"Illinois Valley PADS","Shelter","Ottawa area","Contact outreach to confirm shelter and transportation options."},
    {"Ottawa YMCA","Showers","Ottawa, Illinois","Confirm shower access and fees before visiting."},
    {"Salvation Army","Clothing","Ottawa, Illinois","Ask about clothing and assistance."},
    {"Reddick Public Library","Wi-Fi","Ottawa, Illinois","Check current computer and Wi-Fi hours."},
    {"Illinois Secretary of State","ID Help","Ottawa, Illinois","Confirm required ID documents."},
    {"Illinois workNet","Jobs","Illinois","Employment resources."},
    {"Community Support","Health","Ottawa, Illinois","Dial 211 for community referrals."}
  };
  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
  GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
  @Override public void onCreate(Bundle b){
    super.onCreate(b);
    saved=getSharedPreferences("saved",MODE_PRIVATE);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS|WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    getWindow().setStatusBarColor(BG);
    getWindow().setNavigationBarColor(BG);
    if(Build.VERSION.SDK_INT>=30)getWindow().setDecorFitsSystemWindows(true);
    show("Home");
  }
  TextView label(String s,int size,int color,boolean bold){
    TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
    if(bold)t.setTypeface(null,Typeface.BOLD);
    t.setGravity(Gravity.CENTER_VERTICAL);
    return t;
  }
  LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  LinearLayout.LayoutParams margin(int w,int h,int a,int b,int c,int d){
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));p.setMargins(dp(a),dp(b),dp(c),dp(d));return p;
  }
  void heading(String s){TextView t=label(s,22,WHITE,true);content.addView(t,margin(-1,-2,16,16,16,8));}
  void note(String s){TextView t=label(s,14,0xffb7c3cd,false);content.addView(t,margin(-1,-2,16,0,16,12));}
  void card(String title,String subtitle,Runnable action){
    LinearLayout box=vertical();box.setPadding(dp(16),dp(13),dp(16),dp(13));box.setBackground(shape(PANEL,14));
    box.addView(label(title+"   ›",17,WHITE,true));
    if(subtitle!=null&&!subtitle.isEmpty()){TextView sub=label(subtitle,13,0xffb8c3ce,false);sub.setPadding(0,dp(5),0,0);box.addView(sub);}
    content.addView(box,margin(-1,-2,14,5,14,5));
    if(action!=null)box.setOnClickListener(v->action.run());
  }
  void show(String page){
    root=vertical();root.setBackgroundColor(BG);root.setFitsSystemWindows(true);setContentView(root);
    LinearLayout header=vertical();header.setPadding(dp(18),dp(16),dp(18),dp(12));header.setBackgroundColor(BG);
    TextView logo=label("Street2Home",28,ORANGE,true);header.addView(logo);
    header.addView(label("OTTAWA, ILLINOIS  •  RESOURCES & SUPPORT",10,0xffd0d4da,true));
    root.addView(header);
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);
    content=vertical();content.setPadding(0,0,0,dp(18));scroll.addView(content);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    if(page.equals("Home")){
      TextView banner=label("REAL RESOURCES. A STRONGER TOMORROW.",16,WHITE,true);
      banner.setPadding(dp(18),dp(22),dp(18),dp(22));banner.setBackground(shape(PANEL,16));
      content.addView(banner,margin(-1,-2,14,8,14,12));
      heading("Find help near you");
      String[] cats={"Food","Shelter","Showers","Clothing","Transportation","ID Help","Jobs","Health","Wi-Fi","More"};
      String[] icons={"🍴","⌂","♨","▣","▰","▤","▣","✚","◉","•••"};
      int[] colors={0xffa85c23,0xff465c8e,0xff197d87,0xff714b98,0xff57606b,0xffa7771f,0xff287d56,0xffa44349,0xff397e95,0xff454a57};
      for(int i=0;i<cats.length;i+=2){
        LinearLayout row=new LinearLayout(this);row.setOrientation(0);
        for(int j=i;j<Math.min(i+2,cats.length);j++){
          final String c=cats[j];
          TextView tile=label(icons[j]+"\n"+c,16,WHITE,true);tile.setGravity(Gravity.CENTER);
          tile.setMinHeight(dp(100));tile.setBackground(shape(colors[j],15));
          LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(100),1);
          p.setMargins(dp(5),dp(5),dp(5),dp(5));row.addView(tile,p);
          tile.setOnClickListener(v->{category=c.equals("Transportation")?"Shelter":c.equals("More")?"":c;show(c.equals("More")?"Settings":"Resources");});
        }
        content.addView(row,margin(-1,-2,9,0,9,0));
      }
      note("Resource details are stored offline. Verify availability before traveling.");
    }else if(page.equals("Resources")){
      heading(category.isEmpty()?"All resources":category+" & resources");
      note("Offline directory • Information is not live verified");
      card("Show all categories","Browse every resource",()->{category="";show("Resources");});
      int count=0;
      for(String[] p:places)if(category.isEmpty()||category.equals(p[1])){count++;card(p[0],p[1]+"  •  "+p[2],()->detail(p));}
      if(count==0)card("No local listings yet","Choose Show all categories to browse the directory.",null);
    }else if(page.equals("Saved")){
      heading("Saved Places");int count=0;
      for(String[] p:places)if(saved.getBoolean(p[0],false)){count++;card(p[0],p[1]+" • "+p[2],()->detail(p));}
      if(count==0)note("No saved places yet. Open a resource and tap Save.");
    }else if(page.equals("Map")){
      heading("Resource Map");note("Online maps require a connection. Your resource directory works offline.");
      card("Open Ottawa map","Launch your installed maps application",()->open("geo:0,0?q=Ottawa%2C+Illinois"));
      for(String[] p:places)card(p[0],p[1],()->detail(p));
    }else if(page.equals("Help Now")){
      heading("Help Now");note("Quick access to emergency and support services");
      card("☎  Emergency — 911","Call for immediate danger",()->dial("911"));
      card("♥  Crisis Support — 988","24/7 suicide and crisis lifeline",()->dial("988"));
      card("✚  Community Resources — 211","Ask for local assistance",()->dial("211"));
      card("Illinois Valley PADS","Ask about outreach and transportation",()->{category="Shelter";show("Resources");});
    }else if(page.equals("Jobs & ID Help")){
      heading("Jobs & ID Help");
      card("Local job resources","Illinois workNet • internet required",()->open("https://www.illinoisworknet.com/"));
      card("Replace an Illinois ID","Illinois Secretary of State • internet required",()->open("https://www.ilsos.gov/"));
      card("Transportation assistance","Contact PADS outreach for options",()->{category="Shelter";show("Resources");});
    }else if(page.equals("Settings")){
      heading("Offline & Settings");
      card("Offline resource directory","Included in the app; no connection required",null);
      card("Saved places","Stored on this phone",()->show("Saved"));
      card("Check for updates","New APK versions must be installed separately",null);
      card("About Street2Home","Community resource prototype • details require verification",null);
      card("Jobs & ID Help","Resources and official links",()->show("Jobs & ID Help"));
    }
    navigation=new LinearLayout(this);navigation.setBackgroundColor(0xff121b24);
    String[] tabs={"Home","Resources","Map","Saved","Help Now"};
    String[] symbols={"⌂","☷","⌖","♥","!"};
    for(int i=0;i<tabs.length;i++){
      final String tab=tabs[i];boolean active=page.equals(tab);
      TextView t=label(symbols[i]+"\n"+tab,11,active?ORANGE:WHITE,active);
      t.setGravity(Gravity.CENTER);t.setMinHeight(dp(65));
      navigation.addView(t,new LinearLayout.LayoutParams(0,dp(65),1));
      t.setOnClickListener(v->{if(tab.equals("Resources"))category="";show(tab);});
    }
    root.addView(navigation);
  }
  void detail(String[] p){
    show("Details");
    heading(p[0]);note(p[1]+" • "+p[2]);
    card("About this resource",p[3]+"\nInformation not live-verified.",null);
    card(saved.getBoolean(p[0],false)?"♥ Remove from saved":"♡ Save this place","Keep it in your offline saved list",()->{
      saved.edit().putBoolean(p[0],!saved.getBoolean(p[0],false)).apply();detail(p);
    });
    card("➤ Get directions","Requires a maps application and usually internet",()->open("geo:0,0?q="+Uri.encode(p[0]+" "+p[2])));
    card("← Back to resources","",()->show("Resources"));
  }
  void dial(String number){startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+number)));}
  void open(String uri){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(uri)));}catch(Exception e){Toast.makeText(this,"No compatible app available",Toast.LENGTH_SHORT).show();}}
}
