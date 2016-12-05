package com.example.android.opengl;

/**
 * Created by CaiHaozhong on 2016/11/29.
 */

public class BoundingBox{

    public static final float UNDEFINE = -1;

    public float width = UNDEFINE;

    public float length = UNDEFINE;

    public float heigth = UNDEFINE;

    public Vec3f minPoint;

    public Vec3f maxPoint;

    public Vec3f center;

    public float diagonalLen = UNDEFINE;

    public BoundingBox(){
    }

    public BoundingBox(Vec3f minP, Vec3f maxP){
        minPoint = minP;
        maxPoint = maxP;
        width = maxP.x() - minP.x();
        length = maxP.z() - minP.z();
        heigth = maxP.y() - minP.y();
        center = minP.add(maxP).divByScalar(2);
        diagonalLen = maxP.sub(minP).length();
    }
}