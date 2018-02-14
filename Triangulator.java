package com.vuforia.samples.FloodAR;

import java.util.ArrayList;

class Triangulator
{
	private ArrayList<double []> pv;		// position vectors
	private ArrayList<double []> dv;		// direction vectors

	private double length(double[] vec)
	{
		double a = vec[0]*(vec[0]);
		double b = vec[1]*(vec[1]);
		double c = vec[2]*(vec[2]);
		
		return Math.sqrt(a + b + c);
	}

	private static void rref(double[][] m)
	{
		int lead = 0;
		int rowCount = m.length;
		int colCount = m[0].length;
		int i;
		boolean quit = false;
		
		for(int row = 0; row < rowCount && !quit; row++)
		{			
			if(colCount <= lead)
			{
				quit = true;
				break;
			}
			
			i=row;
			
			while(!quit && m[i][lead] == 0)
			{
				i++;
				if(rowCount == i)
				{
					i=row;
					lead++;
					
					if(colCount == lead)
					{
						quit = true;
						break;
					}
				}
			}

			if(!quit)
			{
				swapRows(m, i, row);
				
				if(m[row][lead] != 0)
				{
					multiplyRow(m, row, 1.0/m[row][lead]);
					m[row][lead] = 1.0;	// explicitly set to 1 to remove rounding errors
				}
				
				for(i = 0; i < rowCount; i++)
				{
					if(i != row)
						subtractRows(m, m[i][lead], row, i);
				}
			}
		}
	}
	
	// swaps two rows
	private static void swapRows(double[][] m, int row1, int row2)
	{
		double [] swap = new double[m[0].length];

		System.arraycopy(m[row1], 0, swap, 0, m[0].length);
				
		for(int c1 = 0; c1 < m[0].length; c1++)
		{
			m[row1][c1] = m[row2][c1];
			m[row2][c1] = swap[c1];
		}
	}
	
	private static void multiplyRow(double[][] m, int row, double scalar)
	{
		for(int c1 = 0; c1 < m[0].length; c1++)
			m[row][c1] = m[row][c1] * scalar;
	}
	
	private static void subtractRows(double[][] m, double scalar, int subtract_scalar_times_this_row, int from_this_row)
	{
		for(int c1 = 0; c1 < m[0].length; c1++)
			m[from_this_row][c1] = m[from_this_row][c1]- (scalar * m[subtract_scalar_times_this_row][c1]);
	}

	double [] triangulate3D(ArrayList<double[]> lines)
	{
		ArrayList<double []> rlist = new ArrayList<>();
		
		// convert lines array to double numbers
		for(double [] vec : lines)
		{
			double [] r = new double[3];

			r[0] = vec[0];
			r[1] = vec[1];
			r[2] = vec[2];
			
			rlist.add(r);
		}
		
		return tri(rlist);
	}

	private double [] tri(ArrayList<double[]> lines)
	{
		double result [] = new double[3];
		double matrix [][] = new double[3][4];
		double len;
		double n;

		pv = new ArrayList<>();
		dv = new ArrayList<>();
		
		pv.clear();
		dv.clear();
		
		for(int c1 = 0; c1 < lines.size(); c1 += 2)
		{
			// create position vectors
			pv.add(new double[3]);
			pv.get(pv.size()-1)[0] = lines.get(c1)[0];
			pv.get(pv.size()-1)[1] = lines.get(c1)[1];
			pv.get(pv.size()-1)[2] = lines.get(c1)[2];
			
			// create unit direction vectors
			dv.add(new double[3]);
			dv.get(dv.size()-1)[0] = lines.get(c1+1)[0] - lines.get(c1)[0];
			dv.get(dv.size()-1)[1] = lines.get(c1+1)[1] - lines.get(c1)[1];
			dv.get(dv.size()-1)[2] = lines.get(c1+1)[2] - lines.get(c1)[2];
			
			len = length(dv.get(dv.size()-1));
			
			dv.get(dv.size()-1)[0] = dv.get(dv.size()-1)[0] / (len);
			dv.get(dv.size()-1)[1] = dv.get(dv.size()-1)[1] / (len);
			dv.get(dv.size()-1)[2] = dv.get(dv.size()-1)[2] / (len);
		}
		
		n = pv.size();
		
		matrix[0][0] = n - sum_dd(0, 0);
		matrix[0][1] = -sum_dd(0, 1);
		matrix[0][2] = -sum_dd(0, 2);
		matrix[0][3] = rhs(0);

		matrix[1][0] = -sum_dd(1, 0);
		matrix[1][1] = n - sum_dd(1, 1);
		matrix[1][2] = -sum_dd(1, 2);
		matrix[1][3] = rhs(1);

		matrix[2][0] = -sum_dd(2, 0);
		matrix[2][1] = -sum_dd(2, 1);
		matrix[2][2] = n - sum_dd(2, 2);
		matrix[2][3] = rhs(2);
		
		rref(matrix);

		result[0] = matrix[0][3];
		result[1] = matrix[1][3];
		result[2] = matrix[2][3];

		return result;
	}

	private double sum_dd(int comp1, int comp2)
	{
		double result = 0.0;
		
		for(double [] v : dv)
			result += v[comp1]*v[comp2];
		
		return result;
	}
	
	private double rhs(int c)
	{
		double result = 0.0;
		double dotproduct;
		
		for(int c1 = 0; c1 < pv.size(); c1++)
		{
			dotproduct = pv.get(c1)[0] * dv.get(c1)[0];
			dotproduct = dotproduct + pv.get(c1)[1]*dv.get(c1)[1];
			dotproduct = dotproduct + pv.get(c1)[2]*dv.get(c1)[2];

			result += (pv.get(c1)[c] - dv.get(c1)[c] * dotproduct);
		}
		
		return result;
	}

	/**
	 * @param p1 the start vector of the line
	 * @param p2 the end vector of the line
	 * @param x the point
	 * @return shortest (perpendicular) distance from point x to line p1 -> p2 as a double
	 */
	double shortestDistance(double[] p1, double[] p2, double[] x)
	{		
		double [] d = new double[3];	// line unit direction vector
		
		d[0] = p2[0] - p1[0];
		d[1] = p2[1] - p1[1];
		d[2] = p2[2] - p1[2];
		
		double len = Math.sqrt(d[0]*d[0] + d[1]*d[1] + d[2]*d[2]);
		
		d[0] /= len;
		d[1] /= len;
		d[2] /= len;

		// (p - x).d
		double pmxdd = (p1[0] - x[0])*d[0] + (p1[1] - x[1])*d[1] + (p1[2] - x[2])*d[2];
		
		double xx = p1[0] - x[0] - d[0] * pmxdd;
		double yy = p1[1] - x[1] - d[1] * pmxdd;
		double zz = p1[2] - x[2] - d[2] * pmxdd;
		
		return Math.sqrt(xx*xx + yy*yy + zz*zz);
	}
}
