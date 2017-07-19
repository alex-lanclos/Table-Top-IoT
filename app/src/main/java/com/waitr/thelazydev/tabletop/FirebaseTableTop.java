package com.waitr.thelazydev.tabletop;

/**
 * Created by alexlanclos on 7/19/17.
 */

import com.google.gson.annotations.SerializedName;


public class FirebaseTableTop {

    @SerializedName("is_occupied")
    private Boolean isOccupied = null;

    @SerializedName("is_ready")
    private Boolean isReady = null;

    @SerializedName("is_reserved")
    private Boolean isReserved = null;

    @SerializedName("table_state")
    private Integer tableState = null;



    public Boolean getIs_occupied() {
        return isOccupied;
    }

    public void setIs_occupied(Boolean isOccupied) {
        this.isOccupied = isOccupied;
    }


    public Boolean getIs_ready() {
        return isReady;
    }

    public void setIs_ready(Boolean isReady) {
        this.isReady = isReady;
    }


    public Boolean getIs_reserved() {
        return isReserved;
    }

    public void setIs_reserved(Boolean isReserved) {
        this.isReserved = isReserved;
    }

    public Integer getTable_state() {
        return tableState;
    }

    public void setTable_state(Integer tableState) {
        this.tableState = tableState;
    }

}
