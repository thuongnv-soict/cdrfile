/**
 *
 */
package cdrfile.global;


/**
 * @author SAMSUNG R430
 *
 */
public class CryptoVietPayProcess {
    private static String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < hex.length() - 1; i += 2) {
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char) decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }


    public static String decryptSMS(String smsContent) {
        String strText = smsContent;
        strText = strText.substring(2);
        String zmk1 = "51FC0185E3F2CCFE";
        String zmk2 = "0C67CE00AC3497F0";

        String icc = strText.substring(0, 40);
        String iccid = convertHexToString(icc);
        iccid = iccid.substring(4);
        String content = strText.substring(48);
        String decrypt = decrypt(content, iccid, zmk1 + zmk2);
        return convertHexToString(decrypt);
    }

    public static String decrypt(String cipherText, String iccid,
                                 String masterKey) {
        String zmk1 = masterKey.substring(0, 16);
        String zmk2 = masterKey.substring(16, 32);
        String zpk1 = CryptoVietPay.des(zmk1, iccid, true, 1,
                                        "0000000000000000");
        String zpk2 = CryptoVietPay.des(zmk2, iccid, true, 1,
                                        "0000000000000000");
        String key = zpk1 + zpk2;
        return CryptoVietPay.des(key, cipherText, false, 1, "0000000000000000");
    }
}
