package threepl.exec;

import java.util.ArrayList;
import java.util.Iterator;

import threepl.exceptions.ExEx;
import threepl.exec.Var.IDtype;
import threepl.nodes.FieldNode;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.SubscriptNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * This class provides a list of resolved subscripts or field
 * names. The subscript or subscript pairs are ints, fields names
 * are Strings and target subscripts are TDEVars. The list is
 * built from a NodeList of the subscript and field nodes which
 * is the subnode of a variable occurrence VarNode. These sub nodes
 * are all evaluated to get the current values for the SubFieldList.
 */
@SuppressWarnings("serial")
public class SubFieldList extends ArrayList<SubField> implements Constant {
    private boolean     target_subs;    // target subscripts in the list?
    private int         scanIndex;      // scanIndex to unscanned tail

    public SubFieldList () {
        super();
        target_subs = false;
    }
    
    /**
     * Construct a subscript/field list containing a single subscript.
     * @param   i is the subscript
     * @param   loc is the source file location
     */
    public SubFieldList (int i,  SrcLoc loc) {
        super();
        target_subs = false;
        add(new SubField(i, 0, loc));
    }
    
    /**
     * Construct a subscript/field list containing a single field.
     * @param   field is the field name string
     * @param   loc is the source file location
     */
    public SubFieldList (String field,  SrcLoc loc) {
        super();
        target_subs = false;
        add(new SubField(field, loc));
    }
    
    /**
     * Construct a subscript/field list from the subnodes or
     * a VarNode (variable occurrence).
     * @param   subs is the list of subscript/field nodes
     * @param   loc is the source file location
     */
    public SubFieldList (
        NodeList        subs,
        SrcLoc          loc
    ) {
        super();
        target_subs = false;
        if (subs == null)
            return;

        // evaluate all the subscripts/field names
        for (int i=0 ; i<subs.size() ; i++) {
            Node    n = subs.get(i);
            if (n instanceof SubscriptNode) {
                // is a subscript or subscript pair
                SubscriptNode   sn = (SubscriptNode)n;
                Node            n1 = null;
                Node            n2 = null;
                if (sn.getNumSubNodes() > 0) {
                    n1 = sn.getSubNode(0);
                    if (sn.getNumSubNodes() > 1)
                        n2 = sn.getSubNode(1);
                }
                SubField    sf = new SubField(n1, n2, true, loc);
                add(sf);
                if (sf.getType() == SFType.TARSUB)
                    target_subs = true;
            } else {
                // is a field identifier string
                FieldNode   fn = (FieldNode)n;
                Node        n1 = fn.getSubNode(0);
                SubField    sf = new SubField(n1, null, false, loc);
                add(sf);
                if (sf.getType() == SFType.TARSUB)
                    target_subs = true;
            }
        }
    }

    /**
     * Merge a subscript/field list to this subscript/field list.
     * @param   var is the parameter variable
     * @param   l2 is the subscript/field list to be merged
     * @return  the merged list
     */
    public SubFieldList merge (Var var, SubFieldList l2) {
        if (l2.size() == 0)
            return(this);
        if (size() == 0)
            return(l2);
        Iterator<SubField>  ai = iterator();    // first list iterator
        int                 pindex = 0;         // second list scanIndex
        SubField            sfl1;
        SubField            sfl2 = l2.get(0);
        SubFieldList        newl = new SubFieldList();
        int                 lower;
        int                 upper;
        
        while (ai.hasNext() || (pindex < l2.size())) {
            if (ai.hasNext()) {
                sfl1 = ai.next();
                if (ai.hasNext() && (sfl2.getType() == SFType.FIELD))
                    throw new ExEx("Parameter '" + var.getID(IDtype.CHAIN) + "' - field identifier in wrong place",
                                                            sfl2.getSrcLoc());
                switch (sfl1.getType()) {
                case NULL:
                    newl.add(sfl2);
                    if (++pindex < l2.size())
                        sfl2 = l2.get(pindex);
                    break;
                case IMSUB:
                    newl.add(sfl1);
                    break;
                case IMSUBS:
                    switch (sfl2.getType()) {
                    case NULL:
                        newl.add(sfl1);
                        break;
                    case IMSUB:
                        lower = sfl1.getLower() + sfl2.getLower();
                        newl.add(new SubField(lower, 0, sfl2.getSrcLoc()));
                        break;
                    case IMSUBS:
                        lower = sfl1.getLower() + sfl2.getLower();
                        upper = sfl1.getLower() + sfl2.getUpper();
                        if (upper > sfl1.getUpper())
                            throw new ExEx("Parameter '" + var.getID(IDtype.CHAIN) + "' - subscript out of range",
                                                            sfl2.getSrcLoc());
                        newl.add(new SubField(lower, upper, sfl2.getSrcLoc()));
                        break;
                    case TARSUB:
                        throw new ExEx("Parameter '" + var.getID(IDtype.CHAIN) + "' - variable subscript on argument subrange",
                                                            sfl2.getSrcLoc());
                    case FIELD:
                        break;
                    case KEY:
                        break;
                    case NONE:
                        break;
                    }
                    if (++pindex < l2.size())
                        sfl2 = l2.get(pindex);
                    break;
                case FIELD:
                    newl.add(sfl1);
		    break;
                case KEY:
                    break;
                case NONE:
                    break;
                case TARSUB:
                    break;
                }
            } else {
                newl.add(sfl2);
                if (++pindex < l2.size())
                    sfl2 = l2.get(pindex);
            }
        }

        if (target_subs || l2.target_subs)
            newl.target_subs = true;

        return(newl);
    }
    
    /**
     * Get an entry from this subscript/field list.
     * @param   i the scanIndex of the required emtry
     * @return  the SubField entry
     */
    public SubField getEntry (int i) {
        return(super.get(i));
    }
    
    /**
     * Get a field name string.
     * @param   i the scanIndex of the required emtry
     * @return  the field name string
     * @throws  ExEx if the SubField is not a field name
     */
    public String getField(int i) {
        return(getEntry(i).getField());
    }
    
    /**
     * Set the scan index.
     * This indexes the first entry which has not yet been scanned in creating a WordSpec.
     * This is called from Type.getWordSpec() as the subfield list is scanned.
     * Scanning stops when a map or list type is encountered.
     * The scanIndex indicates where a map key or map or list scanIndex is located
     * and hence the point at which scanning may continue for types nested in lists or maps.
     * @param   i the scanIndex
     */
    public void setScanIndex (int i) { scanIndex = i; }
    
    /**
     * Increment the scan index.
     * This is called when a list or map scanIndex or key has been used from the list.
     */
    public void incrementScanIndex () { scanIndex++; }
    
    /**
     * Get the scan index.
     * This indexes the last entry which has been scanned.
     * @return   the scan scanIndex
     */
    public int getScanIndex () { return(scanIndex); }
    
    /**
     * Get the list entry pointed to by the scan index.
     * @return   the scan scanIndex list entry
     */
    public SubField getIndexedEntry () { return(get(scanIndex)); }
    
    /**
     * Detect if the scan scanIndex has run off the end of the list.
     * @return   true if the scan scanIndex is equal to or greater than the list size.
     */
    public boolean scanIndexAtEnd () { return(scanIndex >= size()); }
    
    /**
     * Get the tail of the list from the entry following the scan index onwards.
     * The returned list is a shallow copy of the tail of the subject list
     * with scanIndex set to 0.
     */
    public void trimToScanIndex () {
        int n = size();
        
        for (int i=0 ; (i<n)&&(i<scanIndex) ; i++)
            remove(0);
        scanIndex = 0;  
    }

    /**
     * Determine if this subscript/field list contains one or more
     * target subscripts, i.e. subscripts which are value, static or queue
     * mode variables.
     * @return  true if there is one or more target subscripts in the list
     */    
    public boolean hasTargetSubs () {
        return(target_subs);
    }
}
