package de.symeda.sormas.api.campaign;

import java.io.Serializable;
import java.math.BigInteger;

public class CampaignTreeFlatDto implements Serializable {
    private static final long serialVersionUID = 1L;

    // Area
    public String areaName, areaUuid;
    public Long   areaId, areaExtId;

    // Region
    public String regionName, regionUuid;
    public Long   regionId;

    // District
    public String  districtName, districtUuid;
    public Long    districtId;
    public Boolean districtSelected;
    public String  districtModality, districtStatus;

    // Cluster
    public String  clusterName, clusterUuid;
    public Long    clusterId;
    public Boolean clusterSelected;
    public String  clusterModality, clusterFloating, clusterStatus;

    // Population
    public Long pop0_4, pop5_10, pop4_23m, pop4_59m;

    public CampaignTreeFlatDto(Object[] r) {
        if (r == null) {
            throw new IllegalArgumentException("Row array must not be null");
        }

        int i = 0;
        areaName         = str(r, i++);
        areaUuid         = str(r, i++);
        areaId           = bigInt(r, i++);
        areaExtId        = bigInt(r, i++);
        regionName       = str(r, i++);
        regionUuid       = str(r, i++);
        regionId         = bigInt(r, i++);
        districtName     = str(r, i++);
        districtUuid     = str(r, i++);
        districtId       = bigInt(r, i++);
        districtSelected = bool(r, i++);
        districtModality = str(r, i++);
        districtStatus   = str(r, i++);
        clusterName      = str(r, i++);
        clusterUuid      = str(r, i++);
        clusterId        = bigInt(r, i++);
        clusterFloating  = str(r, i++);
        clusterSelected  = bool(r, i++);
        clusterModality  = str(r, i++);
        clusterStatus    = str(r, i++);
        pop0_4           = bigInt(r, i++);
        pop5_10          = bigInt(r, i++);
        pop4_23m         = bigInt(r, i++);
        pop4_59m         = bigInt(r, i++);

    }

    // ── Null-safe helpers ────────────────────────────────────────────────────

    /** Returns the value's toString(), or "" if the element or array is null / out of bounds. */
    private static String str(Object[] r, int i) {
        if (r == null || i >= r.length || r[i] == null) return "";
        return r[i].toString();
    }

    /**
     * Casts to BigInteger and returns its longValue(), or 0L if the element
     * is null, out of bounds, or not a BigInteger/Number type.
     */
    private static Long bigInt(Object[] r, int i) {
        if (r == null || i >= r.length || r[i] == null) return 0L;
        Object o = r[i];
        if (o instanceof BigInteger) return ((BigInteger) o).longValue();
        if (o instanceof Number)     return ((Number) o).longValue();   // safety net
        return 0L;
    }

    /** Returns the Boolean value, or false if the element is null / out of bounds / wrong type. */
    private static Boolean bool(Object[] r, int i) {
        if (r == null || i >= r.length || r[i] == null) return Boolean.FALSE;
        return r[i] instanceof Boolean ? (Boolean) r[i] : Boolean.FALSE;
    }
}