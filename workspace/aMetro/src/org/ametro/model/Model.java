/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.ametro.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Model implements Serializable {

    public static final int VERSION = 1;

    private static final long serialVersionUID = -9024425235347648279L;

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(mMapName);
        out.writeObject(mCountryName);
        out.writeObject(mCityName);
        out.writeInt(mWidth);
        out.writeInt(mHeight);

        out.writeInt(mStationDiameter);
        out.writeInt(mLinesWidth);

        out.writeBoolean(mWordWrap);
        out.writeBoolean(mUpperCase);

        out.writeLong(mTimestamp);
        out.writeLong(mCrc);
        out.writeLong(mSourceVersion);

        out.writeInt(mLines.size());
        for (Line line : mLines.values()) {
            out.writeObject(line);
        }

        out.writeInt(mTransfers.size());
        for (Transfer transfer : mTransfers) {
            out.writeObject(transfer);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

        mMapName = (String) in.readObject();
        mCountryName = (String) in.readObject();
        mCityName = (String) in.readObject();
        mWidth = in.readInt();
        mHeight = in.readInt();

        mStationDiameter = in.readInt();
        mLinesWidth = in.readInt();

        mWordWrap = in.readBoolean();
        mUpperCase = in.readBoolean();

        mTimestamp = in.readLong();
        mCrc = in.readLong();
        mSourceVersion = in.readLong();

        mLines = new HashMap<String, Line>();
        int lineCount = in.readInt();
        for (int i = 0; i < lineCount; i++) {
            Line line = (Line) in.readObject();
            mLines.put(line.getName(), line);
        }

        mTransfers = new ArrayList<Transfer>();
        int transferCount = in.readInt();
        for (int i = 0; i < transferCount; i++) {
            mTransfers.add((Transfer) in.readObject());
        }
    }


    public Model(String mapName) {
        mMapName = mapName;
    }

    public long mTimestamp;
    public long mCrc;

    public String mMapName;

    public String mCityName;
    public String mCountryName;

    public int mWidth;
    public int mHeight;

    public int mStationDiameter;
    public int mLinesWidth;
    public boolean mWordWrap;
    public boolean mUpperCase;

    public long mSourceVersion;

    public HashMap<String, Line> mLines = new HashMap<String, Line>();
    public ArrayList<Transfer> mTransfers = new ArrayList<Transfer>();

    public long getSourceVersion() {
        return mSourceVersion;
    }

    public void setSourceVersion(long mSourceVersion) {
        this.mSourceVersion = mSourceVersion;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public long getCrc() {
        return mCrc;
    }

    public void setCrc(long mCrc) {
        this.mCrc = mCrc;
    }

    public void setCityName(String cityName) {
        mCityName = cityName;
    }

    public String getCityName() {
        return mCityName;
    }

    public void setCountryName(String countryName) {
        mCountryName = countryName;
    }

    public String getCountryName() {
        return mCountryName;
    }

    public boolean isUpperCase() {
        return mUpperCase;
    }

    public void setUpperCase(boolean mUpperCase) {
        this.mUpperCase = mUpperCase;
    }

    public boolean isWordWrap() {
        return mWordWrap;
    }

    public void setWordWrap(boolean mWordWrap) {
        this.mWordWrap = mWordWrap;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getStationDiameter() {
        return mStationDiameter;
    }

    public int getLinesWidth() {
        return mLinesWidth;
    }

    public void setStationDiameter(int mStationDiameter) {
        this.mStationDiameter = mStationDiameter;
    }

    public void setLinesWidth(int mLineWidth) {
        this.mLinesWidth = mLineWidth;
    }

    public String getMapName() {
        return mMapName;
    }

    public void setDimension(int width, int height) {
        mWidth = width;
        mHeight = height;
    }


    public Station getStation(String lineName, String stationName) {
        Line line = mLines.get(lineName);
        if (line != null) {
            return line.getStation(stationName);
        }
        return null;
    }

    public Line getLine(String lineName) {
        return mLines.get(lineName);
    }

    public Transfer addTransfer(Station from, Station to, Double delay, int flags) {
        Transfer tr = new Transfer(from, to, delay, flags);
        mTransfers.add(tr);
        return tr;
    }

    public Line addLine(String lineName, int color, int labelColor, int labelBgColor) {
        Line line = new Line(lineName, color, labelColor, labelBgColor);
        mLines.put(lineName, line);
        return line;
    }

    public Collection<Line> getLines() {
        return mLines.values();
    }

    public Iterator<Transfer> getTransfers() {
        return mTransfers.iterator();
    }


}