/**
 *
 */
package it.eng.d4s.sa3.util;

import java.util.StringTokenizer;

/**
 *
 * This utility class holds a software version in a normalized way.
 *
 * A <code>Version</code> is composed of 4 fields:
 * <ul>
 *  <li><code>major</code></li>
 *  <li><code>minor</code></li>
 *  <li><code>revision</code></li>
 *  <li><code>age</code></li>
 * </ul>
 *
 * @author Gabriele Giammatteo
 *
 */
public class Version {


    private static final String DEFAULT_PARSING_SEPARATORS = "._-";


    private String major    = "0";
    private String minor    = "0";
    private String revision = "0";
    private String age      = "0";

    private String rawRepresentation = "0.0.0.0";


    public Version(final String rawRepresentation){
        this(rawRepresentation, DEFAULT_PARSING_SEPARATORS);
    }

    public Version(final String version, final String separators) {
        this.rawRepresentation = version;
        this.parseRawRepresentation(separators);
    }

    /**
     * @param major
     * @param minor
     * @param revision
     * @param age
     */
    public Version(final String major, final String minor, final String revision, final String age) {
        super();
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.age = age;
        this.rawRepresentation = this.getNormalizedRepresentation();
    }


    public String getMajor() {
        return this.major;
    }
    public String getMinor() {
        return this.minor;
    }
    public String getRevision() {
        return this.revision;
    }
    public String getAge() {
        return this.age;
    }

    public String getNormalizedRepresentation() {
        return this.toString();
    }

    public String getRawRepresentation() {
        return this.rawRepresentation;
    }

    @Override
    public boolean equals(final Object obj) {
       Version v = (Version) obj;

       /*
        * two versions are equals if and only if all four fileds are equals
        * (where fields equality is the String equality)
        */
       return  this.major.equals(v.major)
               &&  this.minor.equals(v.minor)
               &&  this.revision.equals(v.revision)
               &&  this.age.equals(v.age);
    }

    @Override
    public String toString() {
        return
            this.major + "." + this.minor + "." + this.revision + "-"
            + this.age;
    }

    /**
     * TODO: add documentation
     * @param separators
     * @return
     */
    private void parseRawRepresentation(final String separators) {

        if(this.rawRepresentation == null) {
            return;
        }

        StringTokenizer tokenizer =
                new StringTokenizer(this.rawRepresentation, separators);

        if(tokenizer.hasMoreTokens()) {
            this.major = normalizeToken(tokenizer.nextToken());
        } else {
            return;
        }

        if(tokenizer.hasMoreTokens()) {
            this.minor = normalizeToken(tokenizer.nextToken());
        }

        if(tokenizer.hasMoreTokens()) {
            this.revision = normalizeToken(tokenizer.nextToken());
        }

        if(tokenizer.hasMoreTokens()) {
            this.age = normalizeToken(tokenizer.nextToken());
        }
    }

    private static String normalizeToken(final String field){

        String res = field.toLowerCase();

        /*
         * tries to interpret the token as an integer number (as this
         * interpretation is mostly valid for software versions).
         *
         * e.i. the token "001" will be normalized as "1"
         */
        try {
            res = Integer.valueOf(res).toString();
        } catch(Exception e){
            //if String->Integer cast does not success, no matter
        }

        return res;
    }

    public static void main(final String[] args) {
        Version v = new Version("ciao");
        Version v2 = new Version("1.0.1.0",".");
        System.out.println(v);
        System.out.println(v2);
        System.out.println(v.equals(v2));
    }
}
