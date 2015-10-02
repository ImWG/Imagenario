
public class ByteConverter {
	/** 
	  *��32λ��intֵ�ŵ�4�ֽڵ��� 
	  * @param num 
	  * @return 
	  */  
	public static byte[] int2byteArray(int num, boolean reverse) {  
	   byte[] result = new byte[4];  
	   if (reverse){
		   result[3] = (byte)(num >>> 24);//ȡ���8λ�ŵ�0�±�  
		   result[2] = (byte)(num >>> 16);//ȡ�θ�8Ϊ�ŵ�1�±�  
		   result[1] = (byte)(num >>> 8); //ȡ�ε�8λ�ŵ�2�±�  
		   result[0] = (byte)(num );      //ȡ���8λ�ŵ�3�±�  
	   }else{
		   result[0] = (byte)(num >>> 24);//ȡ���8λ�ŵ�0�±�  
		   result[1] = (byte)(num >>> 16);//ȡ�θ�8Ϊ�ŵ�1�±�  
		   result[2] = (byte)(num >>> 8); //ȡ�ε�8λ�ŵ�2�±�  
		   result[3] = (byte)(num );      //ȡ���8λ�ŵ�3�±�   
	   }
	   return result;  
	}  
	  
	/** 
	  * ��4�ֽڵ�byte����ת��һ��intֵ 
	  * @param b 
	  * @return 
	  */  
	public static int byteArray2int(byte[] b, boolean reverse){  
	    byte[] a = new byte[4];  
	    int i = a.length - 1, j;  
	    if (reverse){
	    	for (j = 0; i >= 0; --i, ++j) {//��b��β��(��intֵ�ĵ�λ)��ʼcopy����  
		        if(j < 4)  
		            a[i] = b[j];  
		        else  
		            a[i] = 0;//���b.length����4,�򽫸�λ��0
	    	}
	    }else{
		    for (j = b.length - 1; i >= 0; i--,j--) {//��b��β��(��intֵ�ĵ�λ)��ʼcopy����  
		        if(j >= 0)  
		            a[i] = b[j];  
		        else  
		            a[i] = 0;//���b.length����4,�򽫸�λ��0
		    }
	    }  
	    int v0 = (a[0] & 0xff) << 24;//&0xff��byteֵ�޲���ת��int,����Java�Զ�����������,�ᱣ����λ�ķ���λ  
	    int v1 = (a[1] & 0xff) << 16;  
	    int v2 = (a[2] & 0xff) << 8;  
	    int v3 = (a[3] & 0xff) ;  
	    return v0 | v1 | v2 | v3;  
	}  
	  
	/** 
	  * ת��shortΪbyte 
	  * 
	  * @param b 
	  * @param s ��Ҫת����short 
	  * @param index 
	  */  
	public static void putShort(byte b[], short s, int index) {  
	     b[index + 1] = (byte) (s >> 8);  
	     b[index + 0] = (byte) (s >> 0);  
	}  
	  
	/** 
	  * ͨ��byte����ȡ��short 
	  * 
	  * @param b 
	  * @param index �ڼ�λ��ʼȡ 
	  * @return 
	  */  
	public static short getShort(byte[] b, int index) {  
	      return (short) (((b[index + 1] << 8) | b[index + 0] & 0xff));  
	}  
	  
	/** 
	  * �ַ����ֽ�ת�� 
	  * 
	  * @param ch 
	  * @return 
	  */  
	public static void putChar(byte[] bb, char ch, int index) {  
	        int temp = (int) ch;  
	        // byte[] b = new byte[2];  
	        for (int i = 0; i < 2; i ++ ) {  
	             // �����λ���������λ  
	            bb[index + i] = new Integer(temp & 0xff).byteValue();  
	            temp = temp >> 8; // ������8λ  
	        }  
	}  
	  
	/** 
	  * �ֽڵ��ַ�ת�� 
	  * 
	  * @param b 
	  * @return 
	  */  
	public static char getChar(byte[] b, int index) {  
	        int s = 0;  
	        if (b[index + 1] > 0)  
	            s += b[index + 1];  
	        else  
	            s += 256 + b[index + 0];  
	        s *= 256;  
	        if (b[index + 0] > 0)  
	            s += b[index + 1];  
	        else  
	            s += 256 + b[index + 0];  
	        char ch = (char) s;  
	        return ch;  
	}  
	  
	/** 
	  * floatת��byte 
	  * 
	  * @param bb 
	  * @param x 
	  * @param index 
	  */  
	public static void putFloat(byte[] bb, float x, int index) {  
	        // byte[] b = new byte[4];  
	        int l = Float.floatToIntBits(x);  
	        for (int i = 0; i < 4; i++) {  
	            bb[index + i] = new Integer(l).byteValue();  
	            l = l >> 8;  
	        }  
	}  
	  
	/** 
	  * ͨ��byte����ȡ��float 
	  * 
	  * @param bb 
	  * @param index 
	  * @return 
	  */  
	public static float getFloat(byte[] b, int index) {  
	        int l;  
	        l = b[index + 0];  
	        l &= 0xff;  
	        l |= ((long) b[index + 1] << 8);  
	        l &= 0xffff;  
	        l |= ((long) b[index + 2] << 16);  
	        l &= 0xffffff;  
	        l |= ((long) b[index + 3] << 24);  
	        return Float.intBitsToFloat(l);  
	}  
	  
	/** 
	  * doubleת��byte 
	  * 
	  * @param bb 
	  * @param x 
	  * @param index 
	  */  
	public static void putDouble(byte[] bb, double x, int index) {  
	        // byte[] b = new byte[8];  
	        long l = Double.doubleToLongBits(x);  
	        for (int i = 0; i < 4; i++) {  
	            bb[index + i] = new Long(l).byteValue();  
	            l = l >> 8;  
	        }  
	}  
	  
	/** 
	  * ͨ��byte����ȡ��float 
	  * 
	  * @param bb 
	  * @param index 
	  * @return 
	  */  
	public static double getDouble(byte[] b, int index) {  
	        long l;  
	        l = b[0];  
	        l &= 0xff;  
	        l |= ((long) b[1] << 8);  
	        l &= 0xffff;  
	        l |= ((long) b[2] << 16);  
	        l &= 0xffffff;  
	        l |= ((long) b[3] << 24);  
	        l &= 0xffffffffl;  
	        l |= ((long) b[4] << 32);  
	        l &= 0xffffffffffl;  
	        l |= ((long) b[5] << 40);  
	        l &= 0xffffffffffffl;  
	        l |= ((long) b[6] << 48);  
	        l &= 0xffffffffffffffl;  
	        l |= ((long) b[7] << 56);  
	        return Double.longBitsToDouble(l);  
	    }  
}
