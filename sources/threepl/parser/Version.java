package threepl.parser;

/**
 * Compiler version information.
 *
 * `version' is a string containing the global compiler version - this is in the
 * form MAJOR.MINOR.PATCH, where MAJOR, MINOR and PATCH are positive integers
 * (conforms to Semantic Versioning 2.0.0 - see https://semver.org/). the
 * version number is retrieved by sourcing <b>topdir</b>/version (make or shell)
 *
 * `revision` is a string containing subversion revision information created by
 * running `svnversion <b>topdir</b> /3pl/<b>branch</b>' - where <b>branch</b> could be
 * either /trunk or /branches/mjj-release (depends on where we are working
 * from). if this string contains anything other than digits (e.g. ":", "M",
 * or "S") then the compiler was not built with a clean checked-in tree.
 * i.e. it was not fully updated, there were local modifications, or it does
 * not match the <b>branch</b> above (aka switched).
 *
 * <b>topdir</b> is the path to the root of the subversion working copy, usually
 * something like ../../..
 */
public interface Version {
    public final static String version = "11.4.3";
    public final static String revision = "9164:9205M";
    public final static String last_changed_author = "jen117";
    public final static String last_changed_rev = "9164";
    public final static String last_changed_date = "2020-03-03 23:17:19 +1100";
}
