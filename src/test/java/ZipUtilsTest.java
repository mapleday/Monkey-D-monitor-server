import com.sohu.sns.monitor.util.ZipUtils;

/**
 * Created by Gary Chan on 2016/4/1.
 */
public class ZipUtilsTest {
    public static void main(String[] args) {
        String s = "dsdsgdssssssssssssssssssssssssssssssswer4rgergerhrthgbkdfp[gkdfpogvjrgopjspvmpsdfogjfopsdgjpsaogj" +
                "geksrpgkpasr[gkp[askr[kg" +
                "gkkg0kel;sdgjksdopgjsopdagjdsaoaoaoaoaoaoaoaoaoaoaoaoaoaoaoaoaoaoaoaoaopgjedgjsdofgnsdfoignfsdg" +
                "gsdfjkgpjdfiopgjsdfiosdfiosdfiosdfiosdfiosdfiosdfiosdfiosdfiosdfiogjeiopjsdfpogjdspfgjdsfpgjdfopj" +
                "fjgodsfjgodjfgofdtytyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyererertttttttttttttttttttttttttttttttttttt" +
                "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttjytjytjtyjytrtyjytj" +
                "gllllllllllllllllllllllllllldfsuaidgasuifgasuidfgwiehioqwedhddoqwifheiowfhioefhowcnosahbfowabcoiwbfioewhfweiofhewoi" +
                "cdhsiochdsiochsdioaghoisdavhbdiosfahoidsahiovhdoivhssiodhsaoivhasdovhao";

        System.out.println(s.getBytes().length);

        System.out.println(ZipUtils.gzip(s));
        System.out.println(ZipUtils.gunzip(ZipUtils.gzip(s)));

        System.out.println(ZipUtils.gzip(s).getBytes().length/(double)(s.getBytes().length));
    }
}
