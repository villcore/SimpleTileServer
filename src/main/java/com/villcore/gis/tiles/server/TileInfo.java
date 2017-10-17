package com.villcore.gis.tiles.server;

public class TileInfo {
    private String district;
    private int zLevel;
    private int xLevel;
    private int yLevel;

    public TileInfo(String district, int zLevel, int xLevel, int yLevel) {
        this.district = district;
        this.zLevel = zLevel;
        this.xLevel = xLevel;
        this.yLevel = yLevel;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public int getzLevel() {
        return zLevel;
    }

    public void setzLevel(int zLevel) {
        this.zLevel = zLevel;
    }

    public int getxLevel() {
        return xLevel;
    }

    public void setxLevel(int xLevel) {
        this.xLevel = xLevel;
    }

    public int getyLevel() {
        return yLevel;
    }

    public void setyLevel(int yLevel) {
        this.yLevel = yLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TileInfo tileInfo = (TileInfo) o;

        if (zLevel != tileInfo.zLevel) return false;
        if (xLevel != tileInfo.xLevel) return false;
        if (yLevel != tileInfo.yLevel) return false;
        return district.equals(tileInfo.district);
    }

    @Override
    public int hashCode() {
        int result = district.hashCode();
        result = 31 * result + zLevel;
        result = 31 * result + xLevel;
        result = 31 * result + yLevel;
        return result;
    }

    @Override
    public String toString() {
        return "TileInfo{" +
                "district='" + district + '\'' +
                ", zLevel=" + zLevel +
                ", xLevel=" + xLevel +
                ", yLevel=" + yLevel +
                '}';
    }
}
