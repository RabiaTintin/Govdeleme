
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

    public class MetinEkran extends javax.swing.JFrame {
    static Trie fiil_sozluk_agac=new Trie();
    static Trie isim_sozluk_agac=new Trie();
    static boolean fiil_sozluk_mevcut, isim_sozluk_mevcut;
    static String cikan_ek, ek, durum_eki, iyelik_eki;
            
    private static int minimum(int a, int b, int c) {                            
        return Math.min(Math.min(a, b), c);                                      
    }           
    
    public static int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {      
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];        
                                                                                 
        for (int i = 0; i <= lhs.length(); i++)                                 
            distance[i][0] = i;                                                  
        for (int j = 1; j <= rhs.length(); j++)                                 
            distance[0][j] = j;                                                  
                                                                                 
        for (int i = 1; i <= lhs.length(); i++)                                 
            for (int j = 1; j <= rhs.length(); j++)                             
                distance[i][j] = minimum(                                        
                        distance[i - 1][j] + 1,                                  
                        distance[i][j - 1] + 1,                                  
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));
                                                                                 
        return distance[lhs.length()][rhs.length()];                           
    } 
    
       
    public static String ek_cikar(File in, String str) {
    try {
    Boolean b;
        BufferedReader reader;
        reader= new BufferedReader(new FileReader(in));
        String satir = reader.readLine();
            while (satir!=null) {
                b=str.endsWith(satir);
                  if(b==true)
                   {
                    str=replaceLast(str,satir,"");
                    cikan_ek=satir;
                    break;
                   }
               satir = reader.readLine();
              }
        reader.close();
        } catch(IOException e) {
      }
    return str;
    }

    public static Boolean edilgen_ettirgen_oldurgan_fiil_kontrol(String str) {
        
    // edilgen, ettirgen, oldurgan ve kurallı bileşik fiil gövdeleri sözlükte yer almadığı için kontrol yapılıyor. 
    // edilgen, ettirgen, oldurgan ve kurallı bileşik fiil ekleri anlamda değişmeye sebep olduğundan gövde olarak kabul edilmiştir.
    // Bu eklerin listesi sözlüğün kapsamına göre arttırılabilir ya da azaltılabilir.
    
      Boolean edilgen_ettirgen_oldurgan_fiil = false;
      ek=cikan_ek;
      File edilgen_bilesik_ekler = new File("C:\\Users\\Lenovo\\Documents\\NetBeansProjects\\MetinGovdeleme\\lib\\edilgen_bilesik_ekler.txt");
      String eksiz=ek_cikar(edilgen_bilesik_ekler,str);
        if (fiil_sozluk_agac.search(eksiz)==true) 
        {
        edilgen_ettirgen_oldurgan_fiil=true;
        }
      return edilgen_ettirgen_oldurgan_fiil;
    }    
    
    public static String fiil_ek_kontrol(String str) {
    
    // bazı fiil ekleri ile fiil gövdesi karıştığı için kontrol yapılıyor.
    // (Örn: vurmuş > v ---> vurmuş > vur)
        
    String[] ekler={"ır","ir","ur","ür"};
    String govde=str;
    
    for (String i:ekler)
        {
          if (ek.startsWith(i)==true)
           {
               str=str.concat(ek.substring(0,2));
               if (fiil_sozluk_agac.search(str)==true)
               {govde=str; break;}
               
           }
        }
    return govde;
    }
    
    public static String fiil_ek_kontrol_2(String str) {
    
    // edilgen, ettirgen, oldurgan fiil ekleri ile fiil gövdesi karıştığı için kontrol yapılıyor.
    // (Örn: güldürmüş > güld ---> güldürmüş > güldür)
        
    String[] ekler={"ır","ir","ur","ür"};
    String govde=str;
    Boolean y=false;
    
    for (String i:ekler)
        {
          if (ek.startsWith(i)==true)
           {
            if (str.endsWith("d")==true || str.endsWith("t")==true)
             {
                str=str.concat(ek.substring(0,2));
                y=edilgen_ettirgen_oldurgan_fiil_kontrol(str);
                if (y==true)
                {
                  govde=str;break;
                } 
             }
           }
        }
    return govde;
    }
    
    
    public static Boolean yapim_eki_kontrol(String str) {
        
    // Bazı yapım eki almış türemiş kelimeler sözlükte yer almadığı için bu kontrol yapılıyor. 
    // Bu eklerin listesi sözlüğün kapsamına göre arttırılabilir ya da azaltılabilir.
    // (Örn: akılsız, kitapsız, çiçekçi, okullu vs.)
    
    Boolean yapim_eki = false;
    String[] yapim_ekler={"sız","siz","suz","süz","lık","lik","luk","lük","cı","ci","cu","cü","çı","çi","çu","çü","lı","li","lu","lü"};
        for (String i:yapim_ekler)
        {
            if (str.endsWith(i)==true)
            {
              str=replaceLast(str,i,"");
              isim_sozluk_mevcut=isim_sozluk_agac.search(str);
              if (isim_sozluk_mevcut==true)  
                {
                  yapim_eki=true; break;
                }
            }
        } 
      return yapim_eki;
    }    
    

    public static String ortak_ek_kontrol(String str1, String str2) {
        
    // Hem isim gövdesine hem de fiil gövdesine gelebilen ekler kontrol ediliyor.
    // Sadece fiil gövdesine gelebilen ekleri alan fiiller ayrıştırılıyor.
    // (Örn: söylüyor > söyl > söyle)
    // (Örn: kelimeler > kelim > kelimeler)
        
    String[] ekler={"alar","eler","ın","in","un","ün","a","e"};
    Boolean ortak_ek=false;  
    String yakin=str1;
    for (String i:ekler)
        {
           if (str1.endsWith(i)==true)
           {
            ortak_ek=true; break;
           }
        }
    
           if (ortak_ek==false) 
           {
            str1=enyakin_fiil_govdebul(str2);
            fiil_sozluk_mevcut=fiil_sozluk_agac.search(str1);
            if (fiil_sozluk_mevcut==true)  
                {
                  yakin=str1;
                }
           }
         
      return yakin;
    }  
    
    public static String durum_ek_kontrol(String str1, String str2) {
        
        // (Örn: kedisi > kedis ---> böyle olunca iyelik eki -si bulunamıyor)
        // (Örn: kedisi > kedisi) ---> böyle olunca iyelik eki -si bulunabiliyor)
        
        String[] ekler={"ı","i","u","ü"};
        for (String i:ekler)
        {
            if (str1.endsWith(i)==true && str2.endsWith("s")==true)
            {
            str2=str2.concat(i);
            break;
            }
        } 
      return str2;
    }  
    
    public static String iyelik_ek_kontrol(String str1, String str2) {
        
        // (Örn: sorusuna > sorusun > sorus ---> böyle olunca iyelik eki -su bulunamıyor)
        // (Örn: kasına > kasın > kas ---> isim gövdesi ile karıştırılmamalıdır)
        
        String[] ekler={"ın","in","un","ün"};
        String govde=str2;
        for (String i:ekler)
        {
            if (str1.endsWith(i)==true && str2.endsWith("s")==true)
            {
            str2=replaceLast(str2,"s","");
            isim_sozluk_mevcut=isim_sozluk_agac.search(str2);
            if (isim_sozluk_mevcut==true)  
                {
                  govde=str2; break;
                }
             }
        } 
      return govde;
    }  
      
    public static String yumusama(String str) {
       String yumusak_isim_kok=str;
         if (str.endsWith("ng")==true)
            {
             str=replaceLast(str,"ng","nk");
             isim_sozluk_mevcut=isim_sozluk_agac.search(str);
             if (isim_sozluk_mevcut==true) yumusak_isim_kok=str;
            } 
            else if (str.endsWith("b")==true)
            {
             str=replaceLast(str,"b","p");
             isim_sozluk_mevcut=isim_sozluk_agac.search(str);
             if (isim_sozluk_mevcut==true) yumusak_isim_kok=str;
            }
            else if (str.endsWith("c")==true)
           {
             str=replaceLast(str,"c","ç");
             isim_sozluk_mevcut=isim_sozluk_agac.search(str);
             if (isim_sozluk_mevcut==true) yumusak_isim_kok=str;
            }
            else if (str.endsWith("d")==true)
            {
             str=replaceLast(str,"d","t");
             isim_sozluk_mevcut=isim_sozluk_agac.search(str);
             if (isim_sozluk_mevcut==true) yumusak_isim_kok=str;
            }
            else if (str.endsWith("ğ")==true)
            {
             str=replaceLast(str,"ğ","k");
             isim_sozluk_mevcut=isim_sozluk_agac.search(str);
             if (isim_sozluk_mevcut==true) yumusak_isim_kok=str;
            }
        return yumusak_isim_kok;
    }    
      
    public static String dusme(String str) {
    String govde = str;
    String str1 = str,str2 = str;
    String [] sessiz_harfler= {"b","c","ç","d","f","g","ğ","h","j","k","l","m","n","p","r","s","ş","t","v","y","z"};
    String [] dar_sesli_harfler={"ı","i","u","ü"};
    
    for (String i:sessiz_harfler)
      {
       if (str.endsWith(i)==true)
            {
               str1=replaceLast(str,i,"");
            
               for (String j:sessiz_harfler)
                    {
                        if (str1.endsWith(j)==true)
                        {
                            str2=replaceLast(str1,j,"");
                        }}}}
    int a=govde.length()- 1;
    int b=govde.length()- str2.length();
    if (b==2) 
    {
     String sonharf=(govde.substring(a));
        for (String k:dar_sesli_harfler)
        {
        str=str1.concat(k).concat(sonharf);
        isim_sozluk_mevcut=isim_sozluk_agac.search(str);
        if (isim_sozluk_mevcut==true) govde=str;
        }
        }
      return govde;
    }
          
    public static String ekzarf_eki_cikar(String str) {
     String[] ekler={"casına","cesine","yken","ken","yla","yle","la","le"};
        for (String i:ekler)
        {
            if (str.endsWith(i)==true)
            {
               str=replaceLast(str,i,"");break;
            }
        }
        return str;
    }   
    
    public static String ilgi_eki_cikar(String str) {
      String[] ekler={"ki","kü"};
        for (String i:ekler)
        {
            if (str.endsWith(i)==true)
            {
               str=replaceLast(str,i,"");break;
            }
        }
        return str;
    }    
    
    public static String durum_eki_cikar(String str) {
     String[] ekler={"dan","den","tan","ten","da","de","ın","in","un","ün","ta","te","ya","ye","yı","yi","yu","yü","a","e","ı","i","u","ü"};
        for (String i:ekler)
        {
            if (str.endsWith(i)==true)
            {
               str=replaceLast(str,i,"");
               durum_eki=i;
               break;
            }
        }
        return str;
    }    
    
    public static String iyelik_eki_cikar(String str) {
        String[] ekler={"ımız","ınız","imiz","iniz","ları","leri","umuz","unuz","ümüz","ünüz","mız","miz","muz","müz","nız","niz","nuz","nüz","sın","sin","ım","ın","im","in","sı","si","su","sü","um","un","üm","ün","ı","i","m","n","u","ü"}; 
        for (String i:ekler)
        {
            if (str.endsWith(i)==true)
            {
               str=replaceLast(str,i,"");
               iyelik_eki=i;
               break;
            }
        }
        return str;
    }    
    
    public static String cogul_eki_cikar(String str) {
      String[] ekler={"lar","ler"};
        for (String i:ekler)
        {
            if (str.endsWith(i)==true)
            {
               str=replaceLast(str,i,"");break;
            }
        }
        return str;
    }    
    
    public static Boolean fiilimsi_kontrol(String str) {
        
    // fiilimsi ekleri kelimenin türünü değiştirdiği için yapım eki olarak kabul edilmiştir.
    // Bu nedenle, fiilimsi eki alan kelimeler gövdedir.
    // Sıfat fiil, isim fiil, zarf fiil vs. fiilimsi ekleri bu kapsamdadır.
    
    Boolean fiilimsi = false, x=false, y=false;
    try {
    File fiilimsi_ekler = new File("C:\\Users\\Lenovo\\Documents\\NetBeansProjects\\MetinGovdeleme\\lib\\fiilimsi_ekler.txt");
    BufferedReader reader;
    reader = new BufferedReader(new FileReader(fiilimsi_ekler));
    String satir = reader.readLine();
       while (satir!=null) {
        x=str.endsWith(satir);
          if(x==true)
            {
               String fiil_kok=replaceLast(str,satir,"");
               if (fiil_sozluk_agac.search(fiil_kok)==true) 
               {
                fiilimsi=true;
               }
               else
               {
                y=edilgen_ettirgen_oldurgan_fiil_kontrol(fiil_kok);
                if (y==true)
                    {
                    fiilimsi=true;
                    }
               }
               
            }
           satir = reader.readLine();
          }
        reader.close();
      } catch(IOException e) {}
    return fiilimsi;
    }
    
    public static String fiilimsi_kontrol_2(String str) { 
    if (str.endsWith("ğ")==true)
        {
        str=str.concat(durum_eki.substring(0,1));
        }
      return str;
    }
    
    public static String fiilimsi_kontrol_3(String str) { 
    if (str.endsWith("ğ")==true)
         {
         str=str.concat(iyelik_eki.substring(0,1));
         }
      return str;
    }
    
    public static void metinagaciolustur(File in, Trie agac) {
        
    // isim sözlüğü ve fiil sözlüğü olmak üzere 2 adet sözlük bulunmaktadır.
    // her iki sözlük te metin ağacı veri yapısı ile ön belleğe yerleşir.
    
    try {
         BufferedReader reader;
         reader = new BufferedReader(new FileReader(in));
         String satir = reader.readLine();
            while (satir!=null) {
                agac.insert(satir);
                satir = reader.readLine();
              }
            reader.close();
        } catch(IOException e) {
      }
    }
      
    public static String enyakin_fiil_govdebul(String str) {
        
    // hiç bir adımda gövdesi bulunamayan fiiler bu adımda belli kurallar dahilinde fiil sözlüğünde en yakın gövdeye eşleşir.
    
      try {
      File sozluk = new File("C:\\Users\\Lenovo\\Documents\\NetBeansProjects\\MetinGovdeleme\\lib\\fiil_sozluk.txt");
      BufferedReader reader;
      reader = new BufferedReader(new FileReader(sozluk));
      String satir = reader.readLine();
             while (satir!=null) 
                {
                if (satir.startsWith(str)==true)
                {
                    if (computeLevenshteinDistance(satir,str)==1)
                        { 
                            str=satir;break;
                        }
                }
                
                satir = reader.readLine();
                }
             reader.close();
        } catch(IOException e) {
      }
    return str;
    } 

    public static String enyakin_isim_govdebul(String str) {
        
    // hiç bir adımda gövdesi bulunamayan isimler bu adımda belli kurallar dahilinde isim sözlüğünde en yakın gövdeye eşleşir.
    
      try {
      File sozluk = new File("C:\\Users\\Lenovo\\Documents\\NetBeansProjects\\MetinGovdeleme\\lib\\isim_sozluk.txt");
      BufferedReader reader;
      reader = new BufferedReader(new FileReader(sozluk));
      String satir = reader.readLine();
             while (satir!=null) 
                {
                if (satir.startsWith(str)==true)
                {
                    if (computeLevenshteinDistance(satir,str)==1)
                        { 
                         str=satir;break;
                        }
                }
                satir = reader.readLine();
                }
             reader.close();
        } catch(IOException e) {
      }
    return str;
    }    
    
    public static String enyakin_isim_govdebul_2(String str) {
      try {
      File sozluk = new File("C:\\Users\\Lenovo\\Documents\\NetBeansProjects\\MetinGovdeleme\\lib\\isim_sozluk.txt");
      BufferedReader reader;
      reader = new BufferedReader(new FileReader(sozluk));
      String satir = reader.readLine();
             while (satir!=null) 
                {
                if (satir.startsWith(str)==true)
                {
                    if (computeLevenshteinDistance(satir,str)==2)
                        { 
                        str=satir;break;
                        }
                }
                satir = reader.readLine();
                }
             reader.close();
        } catch(IOException e) {
      }
    return str;
    }       

    public static void dosyayayazdir(String str4) {
    try {
     File govdeler = new File("C:\\Users\\Lenovo\\Documents\\NetBeansProjects\\MetinGovdeleme\\lib\\govdeler.txt");
     if (!govdeler.exists()) {
            govdeler.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(govdeler,true);
        try (BufferedWriter bWriter = new BufferedWriter(fileWriter)) {
            bWriter.write(str4);
            bWriter.newLine();
            bWriter.flush();
        }
       } catch(IOException e) {
     }
    }   

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

    public MetinEkran() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        Label1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setText("Metni oku ve gövdeleri yeni bir dosyaya listele...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(Label1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56)
                .addComponent(Label1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(115, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ZoneId zone1 = ZoneId.of("Europe/Istanbul");
        System.out.println("1. programın başlama zamanı"); //programın başlama zamanı
        LocalTime now1= LocalTime.now(zone1);
        System.out.println(now1);
        try {
        File fiil_sozluk = new File("C:\\Users\\Lenovo\\Documents\\NetBeansProjects\\MetinGovdeleme\\lib\\fiil_sozluk.txt");
        metinagaciolustur(fiil_sozluk, fiil_sozluk_agac);
                
        File isim_sozluk = new File("C:\\Users\\Lenovo\\Documents\\NetBeansProjects\\MetinGovdeleme\\lib\\isim_sozluk.txt");
        metinagaciolustur(isim_sozluk, isim_sozluk_agac);
        
        System.out.println("2. fiil ve isim metin ağaçlarının oluşma zamanı");
        LocalTime now2= LocalTime.now(zone1);
        System.out.println(now2); //fiil ve isim metin ağaçlarının oluşma zamanı
        
        File file = new File("C:\\Users\\Lenovo\\Documents\\NetBeansProjects\\MetinGovdeleme\\lib\\metin.txt");
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(file));
        String satir = reader.readLine();
        String[] kelimeler;
        
    while (satir!=null) {
        kelimeler=satir.replaceAll("\\p{P}","").split("\\s");
         for (String kelime:kelimeler)
          { 
          kelime=kelime.toLowerCase();
          String kelimenin_ilk_hali=kelime;
       
          fiil_sozluk_mevcut=fiil_sozluk_agac.search(kelime);
          if (fiil_sozluk_mevcut==true) {dosyayayazdir(kelime); } // fiil govde (yalın haldeki fiil gövdesi)
          
          else { 
          isim_sozluk_mevcut=isim_sozluk_agac.search(kelime);
          if (isim_sozluk_mevcut==true) {dosyayayazdir(kelime); } // isim govde (yalın haldeki isim gövdesi)
          
             else 
             {
/*yapım*/    Boolean h=yapim_eki_kontrol(kelime);
/*eki*/      if (h==true) {dosyayayazdir(kelime);} //isim govde (sözlükte bulunmayan gövde tespiti)
/*kontrol*/
          else {
          File fiil_ekler = new File("C:\\Users\\Lenovo\\Documents\\NetBeansProjects\\MetinGovdeleme\\lib\\fiil_ekler.txt");
          String kelimenin_eksiz_hali=ek_cikar(fiil_ekler, kelime); // kelimeden fiile gelen ekler varsa çıkarılıyor
          if(kelimenin_eksiz_hali.equals("d")) {String govde=kelimenin_eksiz_hali.replaceAll("d","de");dosyayayazdir(govde); } // istisna duruma ait kural 1
          else if (kelimenin_eksiz_hali.equals("di")) {String govde=kelimenin_eksiz_hali.replaceAll("di","de");dosyayayazdir(govde); } // istisna duruma ait kural 2
          else if (kelimenin_eksiz_hali.endsWith("d")==true) // istisna duruma ait kural 3 - Ünsüz Yumuşaması (Örn: gidiyor -> git)
            {
               String govde=replaceLast(kelimenin_eksiz_hali,"d","t");
               fiil_sozluk_mevcut=fiil_sozluk_agac.search(govde);
               if (fiil_sozluk_mevcut==true) {dosyayayazdir(govde); } // isim govdesinin değişime uğramaması için kontrol yapılıyor (Örn: kalemde->kalemt)
            }   
          
          else 
          {
          fiil_sozluk_mevcut=fiil_sozluk_agac.search(kelimenin_eksiz_hali);
          if (fiil_sozluk_mevcut==true) {dosyayayazdir(kelimenin_eksiz_hali); } // fiil govdesi
              
                else 
                {
/*edilgen */    Boolean a=edilgen_ettirgen_oldurgan_fiil_kontrol(kelimenin_eksiz_hali); 
/*ettirgen*/    if (a==true) 
/*oldurgan*/               
/*fiiller*/     {
                isim_sozluk_mevcut=isim_sozluk_agac.search(kelimenin_eksiz_hali); // istisna durum (Örn: okul> isim gövde:okul, vurul> fiil gövde:vurul) 
                if (isim_sozluk_mevcut==true) {dosyayayazdir(kelimenin_eksiz_hali); } 
                else
                {
                if (kelimenin_eksiz_hali.endsWith("m")==true)
                   {
/*kurallı*/         if (ek.startsWith("ı")==true || ek.startsWith("u")==true) // (Örn: vurulmuyor > fiil gövde: vurulma)
/*bileşik*/         {kelimenin_eksiz_hali=kelimenin_eksiz_hali.concat("a");dosyayayazdir(kelimenin_eksiz_hali);}
/*fiiller*/         else if (ek.startsWith("i")==true || ek.startsWith("ü")==true) // (Örn: güldürmüyor > fiil gövde: güldürme)
                    {kelimenin_eksiz_hali=kelimenin_eksiz_hali.concat("e");dosyayayazdir(kelimenin_eksiz_hali);}
                   }
                  else  dosyayayazdir(kelimenin_eksiz_hali);
                }
                }
                
                else   
                {       
                String fiil_govde=fiil_ek_kontrol(kelimenin_eksiz_hali);
                if (fiil_govde.equals(kelimenin_eksiz_hali)==false) {dosyayayazdir(fiil_govde);}  

                else   
                {       
                String fiil_govde_2=fiil_ek_kontrol_2(kelimenin_eksiz_hali);
                if (fiil_govde_2.equals(kelimenin_eksiz_hali)==false) {dosyayayazdir(fiil_govde_2);}  

                else
                { 
/*ortak ek*/    String enyakin_fiil_govde=ortak_ek_kontrol(kelimenin_ilk_hali,kelimenin_eksiz_hali);
                if (enyakin_fiil_govde.equals(kelimenin_ilk_hali)==false && kelimenin_eksiz_hali.equals(kelimenin_ilk_hali)==false) {dosyayayazdir(enyakin_fiil_govde);} 
        
                else /****** isim soylu kelimelere geçildi. ******/
                {               
/*ek zarf*/     String kelimenin_ekzarfsız_hali=ekzarf_eki_cikar(kelimenin_ilk_hali);
                isim_sozluk_mevcut=isim_sozluk_agac.search(kelimenin_ekzarfsız_hali);
                if (isim_sozluk_mevcut==true) {dosyayayazdir(kelimenin_ekzarfsız_hali);} //isim govde
                
                else
                {
/*ek fiil*/     File ekfiil_ekler = new File("C:\\Users\\Lenovo\\Documents\\NetBeansProjects\\MetinGovdeleme\\lib\\ekfiil_ekler.txt");
                String kelimenin_ekfiilsiz_hali=ek_cikar(ekfiil_ekler,kelimenin_ekzarfsız_hali);
                isim_sozluk_mevcut=isim_sozluk_agac.search(kelimenin_ekfiilsiz_hali);
                if (isim_sozluk_mevcut==true) {dosyayayazdir(kelimenin_ekfiilsiz_hali); } //isim govde
                
                else 
                {
/*yapım*/       Boolean m=yapim_eki_kontrol(kelimenin_ekfiilsiz_hali);
/*eki*/         if (m==true) {dosyayayazdir(kelimenin_ekfiilsiz_hali);} //(Örn: akıllıdır > isim gövde:akıllı)
/*kontrol*/
                else
                {
/*ilgi eki*/    String kelimenin_ilgieksiz_hali=ilgi_eki_cikar(kelimenin_ekfiilsiz_hali);
                isim_sozluk_mevcut=isim_sozluk_agac.search(kelimenin_ilgieksiz_hali);
                if (isim_sozluk_mevcut==true) {dosyayayazdir(kelimenin_ilgieksiz_hali); } //isim govde
                   
                else 
                {
/*fiilimsi*/    Boolean d=fiilimsi_kontrol(kelimenin_ilgieksiz_hali);
/*kontrol*/     if (d==true) {dosyayayazdir(kelimenin_ilgieksiz_hali);}
                
                else
                {    
/*durum eki*/   String kelimenin_durumeksiz_hali=durum_eki_cikar(kelimenin_ilgieksiz_hali);
/*yumuşama*/    kelimenin_durumeksiz_hali=yumusama(kelimenin_durumeksiz_hali);
/*düşme*/       kelimenin_durumeksiz_hali=dusme(kelimenin_durumeksiz_hali);
                isim_sozluk_mevcut=isim_sozluk_agac.search(kelimenin_durumeksiz_hali);
                if (isim_sozluk_mevcut==true) {dosyayayazdir(kelimenin_durumeksiz_hali); } //isim govde
                   
                else 
                {
/*fiilimsi*/    Boolean e=fiilimsi_kontrol(kelimenin_durumeksiz_hali);
/*kontrol*/     if (e==true) {dosyayayazdir(kelimenin_durumeksiz_hali);}
                
                else 
                {
                String fiilimsi2=fiilimsi_kontrol_2(kelimenin_durumeksiz_hali);
                if (fiilimsi2.equals(kelimenin_durumeksiz_hali)==false) {dosyayayazdir(fiilimsi2);}
                                
                else 
                {
                kelimenin_durumeksiz_hali=durum_ek_kontrol(kelimenin_ilgieksiz_hali,kelimenin_durumeksiz_hali);
/*iyelik eki*/  String kelimenin_iyelikeksiz_hali=iyelik_eki_cikar(kelimenin_durumeksiz_hali);
/*yumuşama*/    kelimenin_iyelikeksiz_hali=yumusama(kelimenin_iyelikeksiz_hali);
/*düşme*/       kelimenin_iyelikeksiz_hali=dusme(kelimenin_iyelikeksiz_hali);
                kelimenin_iyelikeksiz_hali=iyelik_ek_kontrol(kelimenin_durumeksiz_hali,kelimenin_iyelikeksiz_hali);
                isim_sozluk_mevcut=isim_sozluk_agac.search(kelimenin_iyelikeksiz_hali);
                if (isim_sozluk_mevcut==true) {dosyayayazdir(kelimenin_iyelikeksiz_hali); } //isim govde
                   
                else 
                {
/*fiilimsi*/    Boolean f=fiilimsi_kontrol(kelimenin_iyelikeksiz_hali);
/*kontrol*/     if (f==true) {dosyayayazdir(kelimenin_iyelikeksiz_hali);}
                
                else 
                {
                String fiilimsi3=fiilimsi_kontrol_3(kelimenin_iyelikeksiz_hali);
                if (fiilimsi3.equals(kelimenin_iyelikeksiz_hali)==false) {dosyayayazdir(fiilimsi3);}
                                
                else 
                {
/*çoğul eki*/   String kelimenin_coguleksiz_hali=cogul_eki_cikar(kelimenin_iyelikeksiz_hali);
                isim_sozluk_mevcut=isim_sozluk_agac.search(kelimenin_coguleksiz_hali);
                if (isim_sozluk_mevcut==true) {dosyayayazdir(kelimenin_coguleksiz_hali); } //isim govde
                
                else 
                {
/*yapım eki*/   Boolean g=yapim_eki_kontrol(kelimenin_coguleksiz_hali);
/*kontrol*/     if (g==true) {dosyayayazdir(kelimenin_coguleksiz_hali);} //isim govde
                
                else
                {
/*fiilimsi*/    Boolean b=fiilimsi_kontrol(kelimenin_coguleksiz_hali);
/*kontrol*/     if (b==true) {dosyayayazdir(kelimenin_coguleksiz_hali);}
               
                else
                {
                Boolean c=edilgen_ettirgen_oldurgan_fiil_kontrol(kelimenin_coguleksiz_hali);
                if (c==true) {dosyayayazdir(kelimenin_coguleksiz_hali);}
                
                else
                {
/*düşme*/       String kelimenin_sesdusmemis_hali=dusme(kelimenin_coguleksiz_hali);
                isim_sozluk_mevcut=isim_sozluk_agac.search(kelimenin_sesdusmemis_hali);
                if (isim_sozluk_mevcut==true) {dosyayayazdir(kelimenin_sesdusmemis_hali); } //isim govde
                
                else
                {
                String enyakin_isim_govde=enyakin_isim_govdebul(kelimenin_coguleksiz_hali);dosyayayazdir(enyakin_isim_govde);
                if (enyakin_isim_govde.equals("")==true) 
                    {
                    String enyakin_isim_govde_2=enyakin_isim_govdebul_2(kelimenin_coguleksiz_hali);
                    dosyayayazdir(enyakin_isim_govde_2);    
                    }
                 }}}}}}}}}}}}}}}}
               }//isim
            }}}}}}}}
          }//for
        satir = reader.readLine();
    }//while  
    
    System.out.println("3. programın bitiş zamanı");
    LocalTime now3= LocalTime.now(zone1);
    System.out.println(now3); //programın bitiş zamanı
    
    System.out.println("------Sonuçlar------");
    System.out.println("1 ve 2 arasında geçen süre : ");
    long secondsBetween = ChronoUnit.SECONDS.between(now1, now2);
    System.out.println(secondsBetween);
    
    System.out.println("2 ve 3 arasında geçen süre : ");
    long secondsBetween1 = ChronoUnit.SECONDS.between(now2, now3);
    System.out.println(secondsBetween1);
    
    System.out.println("1 ve 3 arasında geçen süre : ");
    long secondsBetween2 = ChronoUnit.SECONDS.between(now1, now3);
    System.out.println(secondsBetween2);
    
    
    Label1.setText("İşlem Tamamlandı");
   
    reader.close();   
     
    }catch(IOException e) {
    }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MetinEkran.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MetinEkran.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MetinEkran.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MetinEkran.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MetinEkran().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Label1;
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables
    }
