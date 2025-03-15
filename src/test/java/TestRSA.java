
import java.math.BigInteger;
import java.util.Map;
import java.util.Random;


/**
 * Quick and dirty implementation of the RSA algorithm
 * Read through main() for a breakdown on the RSA workings
 */
public class TestRSA {
    public static void main(String[] args) {
        // 1. Find large primes p and q
        BigInteger p = largePrime(2048);
        BigInteger q = largePrime(2048);

        // 2. Compute n from p and q
        // n is mod for private & public keys, n bit length is key length
        BigInteger n = n(p, q);

        // 3. Compute Phi(n) (Euler's totient function)
        // Phi(n) = (p-1)(q-1)
        // BigIntegers are objects and must use methods for algebraic operations
        BigInteger phi = getPhi(p, q);

        // 4. Find an int e such that 1 < e < Phi(n) 	and gcd(e,Phi) = 1
        BigInteger e = genE(phi);

        // 5. Calculate d where  d ≡ e^(-1) (mod Phi(n))
        BigInteger d = extEuclid(e, phi)[1];

        // Print generated values for reference
        System.out.println("p: " + p);
        System.out.println("q: " + q);
        System.out.println("n: " + n);
        System.out.println("Phi: " + phi);
        System.out.println("e: " + e);
        System.out.println("d: " + d);

        BigInteger test_private_key =new BigInteger("54267817686124417524619028844573070368239702681067300129097506698428292021022854654702691621491597969560998640708090300779360924266753467815070645620930241341877705901130813084212677620929212311175487087249233957281093994143536050355957955947794516925934657870280378825448094938240941594088026042762908139221325606398822661923600127028693274751737329801292122020603423416820080701909456500168292993308371889743560745222275314455945351664728423042965973342614413283953173436270919527448362521826684516217807575254519792321460108218893487832445980527713542826794791374907754311362192348256299557046949347507830245232165667003764204000826083570419176056458453229593789913999055057876451573849970222551547885712586772674670898702102494066944552736706899801279942670248782978991359328443151879119214232581861005493044636593868792381002862257568163606057985393904059564841192107783957346488882624141436073535063012041808577830317846254593790164684779458617290578335528809359028022820125792757143656537823408389345000813369363442649769859077045033583950131746231236774775433151975108370626705281025352566434829723637670658006223478526998058977758301115684391207597090701954163418793680097211803376317849300440188233874038528323046892302525");
        BigInteger test_public_key =new BigInteger("106350678019724765428696721783445046520969532264136932283563603911193490027958511873258456883415452582048500714851472097812403947728322026081487055680681751426586731132430387706363722374852309946560326296035676463102803284380223328877967867888760182895644569433262627326043715979932297347008021773103846640341");
        BigInteger test_n =new BigInteger("898009156449353814834722600799491868857200103104998076960167436513345325231500862535172417749331236983340723672731864913401173713742067811617676084403117306537265151126575218148183185860967071313094105046131776284379307778188456160295999871160567325826552480302438716212490318114847850812131059850005654248612313325714559165180928843494378618009973341046547814187643459758595295520374744374680932216734801598411984133634223735177509137319422122226812121353749680242769469922699599520968035210696579586170687270033055606446801641858894146321447487843378600719675243736842416074020365818477145883940135241442933164702188662254500857032618902116922778035645373492740078521391724826673918757085408511197733206546207634567692030259350667688165945149655717176518902956083214972804124497636834246825979144534957804955177903755970394641310904775006133832645800521193459502202001626584306578140357528153032009393714709949747305624315724171887287510663232296954110171833437931462137383774765922930164940009618515704640645687058906501362652251484173159112952912523808476470875449409757782213391967145000629753432211271881832921201516197270751420030421901963320792881811993157040135459307966252874397633629421632972398296260185380048455961747043");

        // encryption / decryption example
        String message = "Default Message !!!";
        // Convert string to numbers using a cipher
        BigInteger cipherMessage =new BigInteger(message.getBytes());
        // Encrypt the ciphered message
        BigInteger encrypted = encrypt(cipherMessage, test_public_key, test_n);
        // Decrypt the encrypted message
        BigInteger decrypted = decrypt(encrypted, test_private_key, test_n);
        // Uncipher the decrypted message to text
        String restoredMessage =  new String(decrypted.toByteArray());

        System.out.println("Original message: " + message);
        System.out.println("Ciphered: " + cipherMessage);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypted);
        System.out.println("Restored: " + restoredMessage);

        return;
    }


    /**
     * Takes a string and converts each character to an ASCII decimal value
     * Returns BigInteger
     */
    public static BigInteger stringCipher(String message) {
        message = message.toUpperCase();
        String cipherString = "";
        int i = 0;
        while (i < message.length()) {
            int ch = (int) message.charAt(i);
            cipherString = cipherString + ch;
            i++;
        }
        BigInteger cipherBig = new BigInteger(String.valueOf(cipherString));
        return cipherBig;
    }


    /**
     * Takes a BigInteger that is ciphered and converts it back to plain text
     *	returns a String
     */
    public static String cipherToString(BigInteger message) {
        String cipherString = message.toString();
        String output = "";
        int i = 0;
        while (i < cipherString.length()) {
            int temp = Integer.parseInt(cipherString.substring(i, i + 2));
            char ch = (char) temp;
            output = output + ch;
            i = i + 2;
        }
        return output;
    }


    /** 3. Compute Phi(n) (Euler's totient function)
     *  Phi(n) = (p-1)(q-1)
     *	BigIntegers are objects and must use methods for algebraic operations
     */
    public static BigInteger getPhi(BigInteger p, BigInteger q) {
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        return phi;
    }

    /**
     * Generates a random large prime number of specified bitlength
     *
     */
    public static BigInteger largePrime(int bits) {
        Random randomInteger = new Random();
        BigInteger largePrime = BigInteger.probablePrime(bits, randomInteger);
        return largePrime;
    }


    /**
     * Recursive implementation of Euclidian algorithm to find greatest common denominator
     * Note: Uses BigInteger operations
     */
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO)) {
            return a;
        } else {
            return gcd(b, a.mod(b));
        }
    }


    /** Recursive EXTENDED Euclidean algorithm, solves Bezout's identity (ax + by = gcd(a,b))
     * and finds the multiplicative inverse which is the solution to ax ≡ 1 (mod m)
     * returns [d, p, q] where d = gcd(a,b) and ap + bq = d
     * Note: Uses BigInteger operations
     */
    public static BigInteger[] extEuclid(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO)) return new BigInteger[] {
                a, BigInteger.ONE, BigInteger.ZERO
        }; // { a, 1, 0 }
        BigInteger[] vals = extEuclid(b, a.mod(b));
        BigInteger d = vals[0];
        BigInteger p = vals[2];
        BigInteger q = vals[1].subtract(a.divide(b).multiply(vals[2]));
        return new BigInteger[] {
                d, p, q
        };
    }


    /**
     * generate e by finding a Phi such that they are coprimes (gcd = 1)
     *
     */
    public static BigInteger genE(BigInteger phi) {
        Random rand = new Random();
        BigInteger e = new BigInteger(1024, rand);
        do {
            e = new BigInteger(1024, rand);
            while (e.min(phi).equals(phi)) { // while phi is smaller than e, look for a new e
                e = new BigInteger(1024, rand);
            }
        } while (!gcd(e, phi).equals(BigInteger.ONE)); // if gcd(e,phi) isnt 1 then stay in loop
        return e;
    }

    public static BigInteger encrypt(BigInteger message, BigInteger e, BigInteger n) {
        return message.modPow(e, n);
    }

    public static BigInteger decrypt(BigInteger message, BigInteger d, BigInteger n) {
        return message.modPow(d, n);
    }

    public static BigInteger n(BigInteger p, BigInteger q) {
        return p.multiply(q);
    }
}
