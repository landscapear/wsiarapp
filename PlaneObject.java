/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.vuforia.samples.FloodAR;

import java.io.Serializable;
import java.nio.Buffer;

public class PlaneObject extends MeshObject implements Serializable
{    
	private static final long serialVersionUID = 1L;
	
	private transient Buffer mVertBuff;
    private transient Buffer mTexCoordBuff;
    private transient Buffer mNormBuff;
    private transient Buffer mIndBuff;
    
    private transient int indicesNumber = 0;
    private transient int verticesNumber = 0;
    
    public PlaneObject()
    {
    	super();
    	
        setVerts();
        setTexCoords();
        setNorms();
        setIndices();
    }
    
    public PlaneObject(float tx, float ty, float tz, float rx, float ry, float rz, float sx, float sy, float sz)
    {
    	super(tx, ty, tz, rx, ry, rz, sx, sy, sz);

        setVerts();
        setTexCoords();
        setNorms();
        setIndices();
    }
    
    public PlaneObject(PlaneObject c)
    {
    	super(c);
    	
        setVerts();
        setTexCoords();
        setNorms();
        setIndices();
    }

    private void setVerts()
    {
        double[] PLANE_VERTS = { -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f, 0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0.0f };

        mVertBuff = fillBuffer(PLANE_VERTS);
        verticesNumber = PLANE_VERTS.length / 3;
    }

    private void setTexCoords()
    {
        double[] PLANE_TEX_COORDS = { 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f };

        mTexCoordBuff = fillBuffer(PLANE_TEX_COORDS);
    }

    private void setNorms()
    {
        double[] PLANE_NORMS = { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f };

        mNormBuff = fillBuffer(PLANE_NORMS);
    }

    private void setIndices()
    {
        short[] PLANE_INDICES = { 0, 1, 2, 0, 2, 3 };

        mIndBuff = fillBuffer(PLANE_INDICES);
        indicesNumber = PLANE_INDICES.length;
    }
 
   public int getNumObjectIndex()
    {
        return indicesNumber;
    }

    @Override
    public int getNumObjectVertex()
    {
        return verticesNumber;
    }

    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
            default:
                break;
        
        }
        
        return result;
    }

    public PlaneObject copy()
    {
    	return new PlaneObject(this);
    }
}
