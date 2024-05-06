package example;

/*
*直接看入口函数main即可
*/

//传感器参数类定义
class MLX90640_Pars 
{
	public  short kVdd;
	public  short vdd25;
	public double KvPTAT;
	public double KtPTAT;
	public int vPTAT25;
	public double alphaPTAT;
	public short gainEE;
	public double tgc;
	public double cpKv;
	public double cpKta;
	public int resolutionEE;//UINT8
	public int calibrationModeEE;//UINT8
	public double KsTa;
	public double[] ksTo=new double[4];
	public short[] ct=new short[4];
	public double[] alpha=new double[768];
	public short[] offset=new short[768];
	public double[] kta=new double[768];
	public double[] kv=new double[768];
	public double[] cpAlpha=new double[2];
	public short[] cpOffset=new short[2];
	public double[] ilChessC=new double[3];
	public int[] brokenPixels=new int[5];
	public int[] outlierPixels=new int[5];

	public MLX90640_Pars() 
	{
		super();
	}
}

//MLX90640_API类定义
class MLX90640_Funs 
{
	public MLX90640_Pars myPars =new MLX90640_Pars();
	public double mlx90640Tos[]=new double[768];

	public MLX90640_Funs() 
	{
		super();
	}

	short CheckEEPROMValid(int[] eeData)
	{
		int deviceSelect;
		
		deviceSelect = eeData[10] & 0x0040;
		if(deviceSelect == 0)
		{
			return 0;
		}

		return -7;
	}

 	short CheckAdjacentPixels(short pix1, short pix2)
 	{
		short pixPosDif=0;

		pixPosDif = pix1;
		pixPosDif-=pix2;
		if(pixPosDif > -34 && pixPosDif < -30)
		{
			return -6;
		}
		if(pixPosDif > -2 && pixPosDif < 2)
		{
			return -6;
		}
		if(pixPosDif > 30 && pixPosDif < 34)
		{
			return -6;
		}

		return 0;
	}
	void ExtractVDDParameters(int[] eeData)
	{
   		short kVdd;
    	short vdd25;

    	kVdd = (short)eeData[51];

    	kVdd = (short)((eeData[51] & 0xFF00) >> 8);
    	if(kVdd > 127)
    	{
			kVdd = (short)(kVdd - 256);
    	}
    	kVdd = (short)(32 * kVdd);
    	vdd25 = (short)(eeData[51] & 0x00FF);
    	vdd25 = (short)(((vdd25 - 256) << 5) - 8192);

    	myPars.kVdd=kVdd;
    	myPars.vdd25 = vdd25;
	}
	void ExtractPTATParameters(int[] eeData)
	{
    	double KvPTAT;
    	double KtPTAT;
    	short vPTAT25;
    	double alphaPTAT;

    	KvPTAT = (eeData[50] & 0xFC00) >> 10;
    	if(KvPTAT > 31)
    	{
			KvPTAT = KvPTAT - 64;
    	}
    	KvPTAT = KvPTAT/4096;

    	KtPTAT = eeData[50] & 0x03FF;
    	if(KtPTAT > 511)
    	{
        	KtPTAT = KtPTAT - 1024;
    	}
    	KtPTAT = KtPTAT/8;

    	vPTAT25 = (short)eeData[49];

    	alphaPTAT = (eeData[16] & 0xF000) / Math.pow(2, (double)14) + 8.0f;

    	myPars.KvPTAT = KvPTAT;
    	myPars.KtPTAT = KtPTAT;
    	myPars.vPTAT25 = vPTAT25;
    	myPars.alphaPTAT = alphaPTAT;
	}
	void ExtractGainParameters(int[] eeData)
	{
    	short gainEE;

    	gainEE = (short)eeData[48];
    	if(gainEE > 32767)
    	{
        	gainEE = (short)(gainEE -65536);
    	}

    	myPars.gainEE = gainEE;
	}
	void ExtractTgcParameters(int[] eeData)
	{
    	double tgc;
    	tgc = eeData[60] & 0x00FF;
    	if(tgc > 127)
    	{
        	tgc = tgc - 256;
    	}
    	tgc = tgc / 32.0f;

    	myPars.tgc = tgc;
	}
	void ExtractResolutionParameters(int[] eeData)
	{
    	int resolutionEE;
    	resolutionEE = (eeData[56] & 0x3000) >> 12;

    	myPars.resolutionEE = resolutionEE;
	}
	void ExtractKsTaParameters(int[] eeData)
	{
    	double KsTa;
    	KsTa = (eeData[60] & 0xFF00) >> 8;
    	if(KsTa > 127)
    	{
        	KsTa = KsTa -256;
    	}
    	KsTa = KsTa / 8192.0f;

    	myPars.KsTa = KsTa;
	}
	void ExtractKsToParameters(int[] eeData)
	{
    	int KsToScale,i;
    	int step;

    	step = (byte)(((eeData[63] & 0x3000) >> 12) * 10);

    	myPars.ct[0] = -40;
    	myPars.ct[1] = 0;
    	myPars.ct[2] = (short)((eeData[63] & 0x00F0) >> 4);
    	myPars.ct[3] = (short)((eeData[63] & 0x0F00) >> 8);

    	myPars.ct[2] = (short)(myPars.ct[2]*step);
    	myPars.ct[3] = (short)(myPars.ct[2] + myPars.ct[3]*step);

		KsToScale = eeData[63];
		KsToScale &=0x000F;
		KsToScale += 8;
    	KsToScale = 1 << KsToScale;

    	myPars.ksTo[0] = eeData[61] & 0x00FF;
    	myPars.ksTo[1] = (eeData[61] & 0xFF00) >> 8;
    	myPars.ksTo[2] = eeData[62] & 0x00FF;
    	myPars.ksTo[3] = (eeData[62] & 0xFF00) >> 8;


    	for(i = 0; i < 4; i++)
    	{
        	if(myPars.ksTo[i] > 127)
        	{
            	myPars.ksTo[i] = myPars.ksTo[i] -256;
        	}
        	myPars.ksTo[i] = myPars.ksTo[i] / KsToScale;
    	}
	}
	void ExtractAlphaParameters(int[] eeData)
	{
    	int accRow[]=new int[24];
    	int i,j;
    	int accColumn[]=new int[32];
    	int p = 0;
    	int alphaRef;
    	int alphaScale;
    	int accRowScale;
    	int accColumnScale;
    	int accRemScale;


    	accRemScale = eeData[32] & 0x000F;
    	accColumnScale = (eeData[32] & 0x00F0) >> 4;
    	accRowScale = (eeData[32] & 0x0F00) >> 8;
    	alphaScale = ((eeData[32] & 0xF000) >> 12) + 30;
    	alphaRef = eeData[33];

    	for(i = 0; i < 6; i++)
    	{
        	p = i * 4;
        	accRow[p + 0] = (eeData[34 + i] & 0x000F);
        	accRow[p + 1] = (eeData[34 + i] & 0x00F0) >> 4;
        	accRow[p + 2] = (eeData[34 + i] & 0x0F00) >> 8;
        	accRow[p + 3] = (eeData[34 + i] & 0xF000) >> 12;
    	}

    	for(i = 0; i < 24; i++)
    	{
        	if (accRow[i] > 7)
        	{
            	accRow[i] = accRow[i] - 16;
        	}
    	}

    	for(i = 0; i < 8; i++)
    	{
        	p = i * 4;
        	accColumn[p + 0] = (eeData[40 + i] & 0x000F);
        	accColumn[p + 1] = (eeData[40 + i] & 0x00F0) >> 4;
        	accColumn[p + 2] = (eeData[40 + i] & 0x0F00) >> 8;
        	accColumn[p + 3] = (eeData[40 + i] & 0xF000) >> 12;
    	}

    	for(i = 0; i < 32; i ++)
    	{
        	if (accColumn[i] > 7)
        	{
            	accColumn[i] = accColumn[i] - 16;
        	}
    	}

    	for(i = 0; i < 24; i++)
    	{
        	for(j = 0; j < 32; j ++)
        	{
            	p = 32 * i +j;
            	myPars.alpha[p] = (eeData[64 + p] & 0x03F0) >> 4;
            	if (myPars.alpha[p] > 31)
            	{
                	myPars.alpha[p] = myPars.alpha[p] - 64;
            	}
            	myPars.alpha[p] = myPars.alpha[p]*(1 << accRemScale);
            	myPars.alpha[p] = (alphaRef + (accRow[i] << accRowScale) + (accColumn[j] << accColumnScale) + myPars.alpha[p]);
            	myPars.alpha[p] = myPars.alpha[p] / Math.pow(2,(double)alphaScale);
        	}
    	}
	}
	void ExtractOffsetParameters(int[] eeData)
	{
    	int occRow[]=new int[24];
    	int i,j;
    	int occColumn[]=new int[32];
    	int p = 0;
    	short offsetRef;
    	int occRowScale;
    	int occColumnScale;
    	int occRemScale;

    	occRemScale = (eeData[16] & 0x000F);
    	occColumnScale = (eeData[16] & 0x00F0) >> 4;
    	occRowScale = (eeData[16] & 0x0F00) >> 8;
    	offsetRef = (short)eeData[17];
    	if (offsetRef > 32767)
    	{
        	offsetRef = (short)(offsetRef - 65536);
    	}

    	for(i = 0; i < 6; i++)
    	{
        	p = i * 4;
        	occRow[p + 0] = (eeData[18 + i] & 0x000F);
        	occRow[p + 1] = (eeData[18 + i] & 0x00F0) >> 4;
        	occRow[p + 2] = (eeData[18 + i] & 0x0F00) >> 8;
        	occRow[p + 3] = (eeData[18 + i] & 0xF000) >> 12;
    	}

    	for(i = 0; i < 24; i++)
    	{
        	if (occRow[i] > 7)
        	{
            	occRow[i] = occRow[i] - 16;
        	}
    	}

    	for(i = 0; i < 8; i++)
    	{
        	p = i * 4;
        	occColumn[p + 0] = (eeData[24 + i] & 0x000F);
        	occColumn[p + 1] = (eeData[24 + i] & 0x00F0) >> 4;
        	occColumn[p + 2] = (eeData[24 + i] & 0x0F00) >> 8;
        	occColumn[p + 3] = (eeData[24 + i] & 0xF000) >> 12;
    	}

    	for(i = 0; i < 32; i ++)
    	{
        	if (occColumn[i] > 7)
        	{
            	occColumn[i] = occColumn[i] - 16;
        	}
    	}

    	for(i = 0; i < 24; i++)
    	{
        	for(j = 0; j < 32; j ++)
        	{
            	p = 32 * i +j;
            	myPars.offset[p] = (short)((eeData[64 + p] & 0xFC00) >> 10);
            	if (myPars.offset[p] > 31)
            	{
                	myPars.offset[p] = (short)(myPars.offset[p] - 64);
            	}
            	myPars.offset[p] = (short)(myPars.offset[p]*(1 << occRemScale));
            	myPars.offset[p] = (short)((offsetRef + (occRow[i] << occRowScale) + (occColumn[j] << occColumnScale) + myPars.offset[p]));
        	}
    	}
	}
	void ExtractKtaPixelParameters(int[] eeData)
	{
    	int p = 0,i,j;
    	byte KtaRC[]=new byte[4];
    	byte KtaRoCo;
    	byte KtaRoCe;
    	byte KtaReCo;
    	byte KtaReCe;
    	int ktaScale1;
    	int ktaScale2;
    	int split;

    	KtaRoCo = (byte)((eeData[54] & 0xFF00) >> 8);
    	if (KtaRoCo > 127)
    	{
        	KtaRoCo = (byte)(KtaRoCo - 256);
    	}
    	KtaRC[0] = KtaRoCo;

    	KtaReCo = (byte)((eeData[54] & 0x00FF));
    	if (KtaReCo > 127)
    	{
        	KtaReCo = (byte)(KtaReCo - 256);
    	}
    	KtaRC[2] = KtaReCo;

    	KtaRoCe = (byte)((eeData[55] & 0xFF00) >> 8);
    	if (KtaRoCe > 127)
    	{
        	KtaRoCe = (byte)(KtaRoCe - 256);
    	}
    	KtaRC[1] = KtaRoCe;

    	KtaReCe = (byte)((eeData[55] & 0x00FF));
    	if (KtaReCe > 127)
    	{
        	KtaReCe = (byte)(KtaReCe - 256);
    	}
    	KtaRC[3] = KtaReCe;

    	ktaScale1 = ((eeData[56] & 0x00F0) >> 4) + 8;
    	ktaScale2 = (eeData[56] & 0x000F);

   	 	for(i = 0; i < 24; i++)
    	{
        	for(j = 0; j < 32; j ++)
        	{
            	p = 32 * i +j;
            	split = 2*(p/32 - (p/64)*2) + p%2;
            	myPars.kta[p] = (eeData[64 + p] & 0x000E) >> 1;
            	if (myPars.kta[p] > 3)
            	{
                	myPars.kta[p] = myPars.kta[p] - 8;
            	}
            	myPars.kta[p] = myPars.kta[p] * (1 << ktaScale2);
            	myPars.kta[p] = KtaRC[split] + myPars.kta[p];
            	myPars.kta[p] = myPars.kta[p] / Math.pow(2,(double)ktaScale1);
        	}
    	}
	}
	void ExtractKvPixelParameters(int[] eeData)
	{
    	int p = 0,i,j;
    	byte KvT[]=new byte[4];
    	byte KvRoCo;
    	byte KvRoCe;
    	byte KvReCo;
    	byte KvReCe;
    	int kvScale;
    	int split;

    	KvRoCo = (byte)((eeData[52] & 0xF000) >> 12);
    	if (KvRoCo > 7)
    	{
        	KvRoCo = (byte)(KvRoCo - 16);
    	}
    	KvT[0] = KvRoCo;

    	KvReCo = (byte)((eeData[52] & 0x0F00) >> 8);
    	if (KvReCo > 7)
    	{
        	KvReCo = (byte)(KvReCo - 16);
    	}
    	KvT[2] = KvReCo;

    	KvRoCe = (byte)((eeData[52] & 0x00F0) >> 4);
    	if (KvRoCe > 7)
    	{
        	KvRoCe = (byte)(KvRoCe - 16);
    	}
    	KvT[1] = KvRoCe;

    	KvReCe = (byte)((eeData[52] & 0x000F));
    	if (KvReCe > 7)
    	{
        	KvReCe = (byte)(KvReCe - 16);
    	}
    	KvT[3] = KvReCe;

    	kvScale = (eeData[56] & 0x0F00) >> 8;


    	for(i = 0; i < 24; i++)
    	{
        	for(j = 0; j < 32; j ++)
        	{
            	p = 32 * i +j;
            	split = 2*(p/32 - (p/64)*2) + p%2;
            	myPars.kv[p] = KvT[split];
            	myPars.kv[p] = myPars.kv[p] / Math.pow(2,(double)kvScale);
        	}
    	}
	}
	void ExtractCPParameters(int[] eeData)
	{
    	double alphaSP[]=new double[2];
    	short offsetSP[]=new short[2];
    	double cpKv;
    	double cpKta;
    	int alphaScale;
    	int ktaScale1;
    	int kvScale;

    	alphaScale = ((eeData[32] & 0xF000) >> 12) + 27;

    	offsetSP[0] = (short)(eeData[58] & 0x03FF);
    	if (offsetSP[0] > 511)
    	{
        	offsetSP[0] = (short)(offsetSP[0] - 1024);
    	}

    	offsetSP[1] = (short)((eeData[58] & 0xFC00) >> 10);
    	if (offsetSP[1] > 31)
    	{
        	offsetSP[1] = (short)(offsetSP[1] - 64);
    	}
    	offsetSP[1] = (short)(offsetSP[1] + offsetSP[0]);

    	alphaSP[0] = (eeData[57] & 0x03FF);
    	if (alphaSP[0] > 511)
    	{
        	alphaSP[0] = alphaSP[0] - 1024;
    	}
    	alphaSP[0] = alphaSP[0] /  Math.pow(2,(double)alphaScale);

    	alphaSP[1] = (eeData[57] & 0xFC00) >> 10;
    	if (alphaSP[1] > 31)
    	{
        	alphaSP[1] = alphaSP[1] - 64;
    	}
    	alphaSP[1] = (1 + alphaSP[1]/128) * alphaSP[0];

    	cpKta = (eeData[59] & 0x00FF);
   	 	if (cpKta > 127)
    	{
        	cpKta = cpKta - 256;
    	}
    	ktaScale1 = ((eeData[56] & 0x00F0) >> 4) + 8;
    	myPars.cpKta = cpKta / Math.pow(2,(double)ktaScale1);

    	cpKv = (eeData[59] & 0xFF00) >> 8;
    	if (cpKv > 127)
    	{
        	cpKv = cpKv - 256;
    	}
    	kvScale = (eeData[56] & 0x0F00) >> 8;
    	myPars.cpKv = cpKv / Math.pow(2,(double)kvScale);

    	myPars.cpAlpha[0] = alphaSP[0];
    	myPars.cpAlpha[1] = alphaSP[1];
    	myPars.cpOffset[0] = offsetSP[0];
    	myPars.cpOffset[1] = offsetSP[1];
	}
	void ExtractCILCParameters(int[] eeData)
	{
    	double ilChessC[]=new double[3];
    	int calibrationModeEE;

    	calibrationModeEE = (eeData[10] & 0x0800) >> 4;
    	calibrationModeEE = calibrationModeEE ^ 0x80;

    	ilChessC[0] = (eeData[53] & 0x003F);
    	if (ilChessC[0] > 31)
    	{
        	ilChessC[0] = ilChessC[0] - 64;
    	}
    	ilChessC[0] = ilChessC[0] / 16.0f;

    	ilChessC[1] = (eeData[53] & 0x07C0) >> 6;
    	if (ilChessC[1] > 15)
    	{
        	ilChessC[1] = ilChessC[1] - 32;
    	}
    	ilChessC[1] = ilChessC[1] / 2.0f;

    	ilChessC[2] = (eeData[53] & 0xF800) >> 11;
    	if (ilChessC[2] > 15)
    	{
        	ilChessC[2] = ilChessC[2] - 32;
    	}
    	ilChessC[2] = ilChessC[2] / 8.0f;

    	myPars.calibrationModeEE = calibrationModeEE;
    	myPars.ilChessC[0] = ilChessC[0];
    	myPars.ilChessC[1] = ilChessC[1];
    	myPars.ilChessC[2] = ilChessC[2];
	}


	short ExtractDeviatingPixels(int[] eeData)
	{
		int pixCnt = 0;
		int brokenPixCnt = 0;
		int outlierPixCnt = 0;
		int warn = 0;
		int i;

		for(pixCnt = 0; pixCnt<5; pixCnt++)
		{
			myPars.brokenPixels[pixCnt] = 0xFFFF;
			myPars.outlierPixels[pixCnt] = 0xFFFF;
		}

		pixCnt = 0;
		while (pixCnt < 768 && brokenPixCnt < 5 && outlierPixCnt < 5)
		{
			if(eeData[pixCnt+64] == 0)
			{
				myPars.brokenPixels[brokenPixCnt] = pixCnt;
				brokenPixCnt = brokenPixCnt + 1;
			}
			else if((eeData[pixCnt+64] & 0x0001) != 0)
			{
				myPars.outlierPixels[outlierPixCnt] = pixCnt;
				outlierPixCnt = outlierPixCnt + 1;
			}

			pixCnt = pixCnt + 1;

		}

		if(brokenPixCnt > 4)
		{
			warn = -3;
		}
		else if(outlierPixCnt > 4)
		{
			warn = -4;
		}
		else if((brokenPixCnt + outlierPixCnt) > 4)
		{
			warn = -5;
		}
		else
		{
			for(pixCnt=0; pixCnt<brokenPixCnt; pixCnt++)
			{
				for(i=pixCnt+1; i<brokenPixCnt; i++)
				{
					warn = CheckAdjacentPixels((short)myPars.brokenPixels[pixCnt],(short)myPars.brokenPixels[i]);
					if(warn != 0)
					{
						return (short)warn;
					}
				}
			}

			for(pixCnt=0; pixCnt<outlierPixCnt; pixCnt++)
			{
				for(i=pixCnt+1; i<outlierPixCnt; i++)
				{
					warn = CheckAdjacentPixels((short)myPars.outlierPixels[pixCnt],(short)myPars.outlierPixels[i]);
					if(warn != 0)
					{
						return (short)warn;
					}
				}
			}

			for(pixCnt=0; pixCnt<brokenPixCnt; pixCnt++)
			{
				for(i=0; i<outlierPixCnt; i++)
				{
					warn = CheckAdjacentPixels((short)myPars.brokenPixels[pixCnt],(short)myPars.outlierPixels[i]);
					if(warn != 0)
					{
						return (short)warn;
					}
				}
			}

		}

		return (short)warn;
	}
	int MLX90640_ExtractParameters(int[] eeData)
	{
    	int error = CheckEEPROMValid(eeData);

    	if(error == 0)
    	{
        	ExtractVDDParameters(eeData);
        	ExtractPTATParameters(eeData);
        	ExtractGainParameters(eeData);
        	ExtractTgcParameters(eeData);
        	ExtractResolutionParameters(eeData);
        	ExtractKsTaParameters(eeData);
        	ExtractKsToParameters(eeData);
        	ExtractAlphaParameters(eeData);
        	ExtractOffsetParameters(eeData);
        	ExtractKtaPixelParameters(eeData);
        	ExtractKvPixelParameters(eeData);
        	ExtractCPParameters(eeData);
        	ExtractCILCParameters(eeData);
        	error = ExtractDeviatingPixels(eeData);
    	}

    	return error;
	}

	double MLX90640_GetVdd(int[] frameData)
	{
    	double vdd;
    	double resolutionCorrection;
    	int resolutionRAM;

    	vdd = frameData[810];
    	if(vdd > 32767)
    	{
        	vdd = vdd - 65536;
    	}

    	resolutionRAM = ((frameData[832] & 0x0C00) >> 10);
    	resolutionCorrection = Math.pow(2, (double)myPars.resolutionEE) / Math.pow(2, (double)resolutionRAM);
    	vdd = (resolutionCorrection * vdd - myPars.vdd25) / myPars.kVdd + 3.3;

    	//System.out.println( "Vdd="+vdd);

    	return vdd;
	}
	double MLX90640_GetTa(int[] frameData)
	{
    	double ptat;
    	double ptatArt;
    	double vdd;
    	double ta;

    	vdd = MLX90640_GetVdd(frameData);

    	ptat = frameData[800];
    	if(ptat > 32767)
    	{
        	ptat = ptat - 65536;
    	}

    	ptatArt = frameData[768];
    	if(ptatArt > 32767)
    	{
        	ptatArt = ptatArt - 65536;
    	}
    	ptatArt = (ptat / (ptat * myPars.alphaPTAT + ptatArt)) * Math.pow(2, (double)18);

    	ta = (ptatArt / (1 + myPars.KvPTAT * (vdd - 3.3)) - myPars.vPTAT25);
    	ta = ta / myPars.KtPTAT + 25;

    	return ta;
	}
	void MLX90640_CalculateTo(int[] frameData, double emissivity, double tr)
	{
    	double vdd;
    	double ta;
    	double ta4;
    	double tr4;
    	double taTr;
    	double gain;
    	double irDataCP[]=new double[2];
    	double irData;
    	double alphaCompensated;
    	int mode;
    	byte ilPattern;
    	byte chessPattern;
    	byte pattern;
    	byte conversionPattern;
    	double Sx;
    	double To;
    	double alphaCorrR[]=new double[4];
    	byte range;
    	int subPage;
		int i,pixelNumber;

    	subPage = frameData[833];
    	vdd = MLX90640_GetVdd(frameData);
    	ta = MLX90640_GetTa(frameData);
    	ta4 = Math.pow((ta + 273.15), (double)4);
    	tr4 = Math.pow((tr + 273.15), (double)4);
    	taTr = tr4 - (tr4-ta4)/emissivity;

    	alphaCorrR[0] = 1 / (1 + myPars.ksTo[0] * 40);
    	alphaCorrR[1] = 1 ;
    	alphaCorrR[2] = (1 + myPars.ksTo[2] * myPars.ct[2]);
    	alphaCorrR[3] = alphaCorrR[2] * (1 + myPars.ksTo[3] * (myPars.ct[3] - myPars.ct[2]));

//------------------------- Gain calculation -----------------------------------
    	gain = frameData[778];
    	if(gain > 32767)
    	{
        	gain = gain - 65536;
    	}

    	gain = myPars.gainEE / gain;

//------------------------- To calculation -------------------------------------
    	mode = (frameData[832] & 0x1000) >> 5;

    	irDataCP[0] = frameData[776];
    	irDataCP[1] = frameData[808];
    	for(i = 0; i < 2; i++)
    	{
       	 if(irDataCP[i] > 32767)
        	{
            	irDataCP[i] = irDataCP[i] - 65536;
        	}
        	irDataCP[i] = irDataCP[i] * gain;
    	}
    	irDataCP[0] = irDataCP[0] - myPars.cpOffset[0] * (1 + myPars.cpKta * (ta - 25)) * (1 + myPars.cpKv * (vdd - 3.3));
    	if( mode ==  myPars.calibrationModeEE)
    	{
        	irDataCP[1] = irDataCP[1] - myPars.cpOffset[1] * (1 + myPars.cpKta * (ta - 25)) * (1 + myPars.cpKv * (vdd - 3.3));
    	}
    	else
    	{
      		irDataCP[1] = irDataCP[1] - (myPars.cpOffset[1] + myPars.ilChessC[0]) * (1 + myPars.cpKta * (ta - 25)) * (1 + myPars.cpKv * (vdd - 3.3));
    	}

    	for(pixelNumber = 0; pixelNumber < 768; pixelNumber++)
    	{
        	ilPattern = (byte)(pixelNumber / 32 - (pixelNumber / 64) * 2);
        	chessPattern = (byte)(ilPattern ^ (pixelNumber - (pixelNumber/2)*2));
        	conversionPattern = (byte)(((pixelNumber + 2) / 4 - (pixelNumber + 3) / 4 + (pixelNumber + 1) / 4 - pixelNumber / 4) * (1 - 2 * ilPattern));

        	if(mode == 0)
        	{
          		pattern = ilPattern;
        	}
        	else
        	{
          		pattern = chessPattern;
        	}

        	if(pattern == frameData[833])
        	{
            	irData = frameData[pixelNumber];
            	if(irData > 32767)
            	{
                	irData = irData - 65536;
            	}
            	irData = irData * gain;

            	irData = irData - myPars.offset[pixelNumber]*(1 + myPars.kta[pixelNumber]*(ta - 25))*(1 + myPars.kv[pixelNumber]*(vdd - 3.3));
            	if(mode !=  myPars.calibrationModeEE)
            	{
              	irData = irData + myPars.ilChessC[2] * (2 * ilPattern - 1) - myPars.ilChessC[1] * conversionPattern;
            	}

            	irData = irData / emissivity;

            	irData = irData - myPars.tgc * irDataCP[subPage];

            	alphaCompensated = (myPars.alpha[pixelNumber] - myPars.tgc * myPars.cpAlpha[subPage])*(1 + myPars.KsTa * (ta - 25));

            	Sx = Math.pow(alphaCompensated, (double)3) * (irData + alphaCompensated * taTr);
            	Sx = Math.sqrt(Math.sqrt(Sx)) * myPars.ksTo[1];

            	To = Math.sqrt(Math.sqrt(irData/(alphaCompensated * (1 - myPars.ksTo[1] * 273.15) + Sx) + taTr)) - 273.15;

            	if(To < myPars.ct[1])
            	{
                	range = 0;
            	}
            	else if(To < myPars.ct[2])
            	{
                	range = 1;
            	}
            	else if(To < myPars.ct[3])
            	{
                	range = 2;
            	}
            	else
            	{
                	range = 3;
            	}

            	To = Math.sqrt(Math.sqrt(irData / (alphaCompensated * alphaCorrR[range] * (1 + myPars.ksTo[range] * (To - myPars.ct[range]))) + taTr)) - 273.15;

            	mlx90640Tos[pixelNumber] = To;
        	}
    	}
	}

}

public class Example
{
   public static void main(String[] args)
   {      
      int[] EEMDatas = {
0x00B6,0x699F,0x0000,0x2061,0x0004,0x0320,0x03E0,0x1A20,0x0206,0x0187,0x0499,0x0000,0x1901,0x0000,0x0000,0xBE33,0x4320,0xFFAC,0x0202,0x0202,0x0102,0xF1F1,0xE0E1,0xBFD0,0x0F00,0x0000,0x0001,0x0101,0xF102,0xF1F2,0xE0F2,0xC0E1,
0x8895,0x36D8,0xEECB,0x2100,0x3332,0x2233,0x0011,0xBBEE,0xCCBA,0xFFEE,0x2111,0x2222,0x2233,0x1222,0x0011,0xDDFF,0x174E,0x2FA7,0x2559,0xA182,0x4333,0x09CD,0x4843,0x4341,0x2352,0xF8CB,0x17AD,0x032F,0xF600,0x9797,0x9797,0x2AFA,
0x001E,0xFBF0,0x23C0,0xF40E,0x07EE,0x0042,0x140E,0xF02E,0x0780,0xF840,0x1850,0xEF6E,0x005E,0xF890,0x0890,0xE4AE,0x03A0,0xF3E0,0x0860,0xEC8E,0xF480,0xF860,0x03C0,0xE09E,0xF092,0xE490,0x0460,0xE00E,0xE4CE,0xE430,0x00B0,0xD89E,
0x13F2,0x07EE,0x13BE,0x1BE0,0x1BE2,0x0C30,0x040E,0x1810,0x1B82,0x042E,0x086E,0x1B30,0x1452,0x0870,0xFC7E,0x1060,0x1F84,0x07BE,0x004E,0x1C40,0x0C52,0x0C2E,0xFFBE,0x1460,0x1062,0xFC6E,0x043E,0x17D0,0x04A2,0x0010,0xFCAE,0x0C60,
0xFBCE,0xF7B0,0x1BB0,0xEFC0,0x0B8E,0xFFF2,0x1000,0xEC2E,0x034E,0xF400,0x1420,0xEB6E,0x0020,0xF480,0x0440,0xE03E,0xF7D0,0xEFB0,0x0BF0,0xE81E,0xF410,0xFBE0,0x0720,0xE02E,0xF400,0xE800,0x0BB0,0xE3DE,0xE840,0xE400,0xFCB0,0xDC2E,
0x1022,0x000E,0x082E,0x1810,0x23E2,0x0C50,0x046E,0x1872,0x1BB2,0x0860,0x089E,0x17B0,0x1882,0x04DE,0xFCAE,0x1080,0x1032,0x0410,0x006E,0x1C60,0x1052,0x1020,0x037E,0x1460,0x1454,0x0050,0x0800,0x1422,0x0852,0x0040,0xFD1E,0x1070,
0x03D0,0xF7C0,0x1BA0,0xEC1E,0x13C0,0x03E0,0x1B80,0xEC4E,0x0B80,0xFBF0,0x1410,0xEB70,0x0450,0xF830,0x0040,0xE7FE,0xFF60,0xF720,0x0BA0,0xF3FE,0xFBF2,0xF850,0x0700,0xEBE0,0xF012,0xEFA0,0x07D0,0xE3CE,0xE48E,0xEBE0,0x0412,0xE00E,
0x1042,0xF81E,0x040E,0x0C60,0x1C12,0x0C30,0x0BEE,0x1090,0x1BE2,0x0450,0x087E,0x0FA0,0x1892,0x047E,0xF48E,0x1030,0x13D4,0x0770,0x03FE,0x1820,0x1054,0x089E,0xFF5E,0x1412,0x0C54,0x03E0,0x0410,0x0C00,0x00D2,0xFC20,0x0070,0x0C40,
0x03C0,0xFF72,0x1400,0xEC3E,0x0040,0x0062,0x1080,0xE8EE,0x03E0,0xF882,0x14A0,0xE7E0,0xFC40,0xF860,0x0820,0xE450,0xFB72,0xF792,0x0430,0xE87E,0xF422,0x03B2,0x0360,0xE43E,0xF7F2,0xE820,0x0BB2,0xE7A0,0xEC20,0xEFF0,0x0452,0xE03E,
0x0B92,0xFF20,0xFFEE,0x0FF2,0x0C12,0x0030,0xF86E,0x0882,0x0FA2,0xFC40,0xFC6E,0x0B70,0x0C02,0x0020,0xF3FE,0x0802,0x0F34,0xFF40,0xF7FE,0x1030,0x0BE4,0x0B80,0xF34E,0x13D0,0x0FC4,0xFBF0,0xFF9E,0x1350,0x07E2,0xFFA0,0xF800,0x0FF0,
0xF7E0,0xF7C2,0x17F0,0xE470,0x0020,0xF8B2,0x08B0,0xE8D0,0xFBFE,0xF850,0x1040,0xE7A0,0xF450,0xF832,0x0042,0xE020,0xF3F0,0xF3C2,0x0012,0xE87E,0xEC62,0xFC10,0xF7E0,0xE7F0,0xF3C2,0xE812,0x07E0,0xE750,0xEFD2,0xE812,0xFCC2,0xE7F0,
0xFB92,0xEF60,0xF7CE,0xF832,0x07F2,0xF460,0xE87E,0x0082,0x03B2,0xF400,0xF40E,0x0350,0x0002,0xFFDE,0xE81E,0x03C0,0xFFB2,0xF770,0xEFDE,0x0800,0xFC24,0x03C0,0xE79E,0x0B82,0x0764,0xF7C0,0xF790,0x0F10,0x0784,0xF7C0,0xF070,0x0FA2,
0x1F80,0x1752,0x3330,0x043E,0x2BD0,0x1482,0x2440,0x0480,0x1BD2,0x17D2,0x2800,0x0360,0x1C10,0x1422,0x1820,0xFC20,0x1762,0x0FB2,0x1800,0x0FA0,0x0C12,0x1420,0x1770,0x03C0,0x0B92,0x0F62,0x1B90,0xFF40,0xFC30,0x07B0,0x1442,0xFFF0,
0xFBA2,0xE780,0xF36E,0xF052,0x0BF2,0xEC90,0xE48E,0xF8B2,0xF802,0xF010,0xEC3E,0xF780,0x0042,0xEC50,0xDC5E,0xF440,0xFF74,0xEFD0,0xE03E,0x0BC0,0xF432,0xF450,0xE3AE,0x03F0,0xFBC2,0xF390,0xEFCE,0x0360,0xF062,0xF3D0,0xE870,0xFC10,
0x03A0,0x0F82,0x27A0,0x03E0,0x0C00,0x1052,0x1870,0xF8E0,0x0400,0x13F2,0x2020,0xFB50,0x0470,0x0C42,0x1002,0xF430,0x0F22,0x07C2,0x17E0,0x0010,0x0020,0x1012,0x1322,0xF430,0x03C2,0x0B52,0x1F42,0xFF20,0xF820,0x03F2,0x1062,0xFBFE,
0xFFD2,0xF3A0,0xFBDE,0x0800,0x0812,0x0070,0xF09E,0x04D0,0x0432,0x0410,0xFC4E,0x1350,0x0490,0x0460,0xF03E,0x0840,0x1352,0xFFE0,0xF41E,0x1810,0x0844,0x0C40,0xF76E,0x1052,0x0C04,0x0B70,0x0B6E,0x1F40,0x0842,0x0400,0xFC80,0x1810,
0xF7DE,0x0B92,0x1BE0,0xFBF0,0x0020,0x0C52,0x0CC0,0xF4B0,0xF870,0x0852,0x10D0,0xF3B0,0xFC80,0x0492,0x0840,0xECC0,0xFF80,0xFFD0,0x0820,0xFC10,0xFC32,0x0C22,0x0790,0xF050,0xFBF2,0x03C2,0x0C10,0xF790,0xF420,0x03F0,0x0872,0xF04E,
0xEFE2,0xEF80,0xE7FE,0xFFE2,0xFC32,0xF460,0xE0DE,0x00A0,0xF092,0xF450,0xE50E,0xFFD2,0xFCA2,0xF8A0,0xE46E,0xFCC0,0xFFB2,0xF7D0,0xE45E,0x0C20,0x0052,0x0430,0xE7BE,0x0870,0x0F94,0xFFC0,0xF03E,0x13A0,0x0432,0xFC00,0xF09E,0x1030,
0xFF80,0xFFE2,0x1390,0xEC40,0xFC00,0xFC72,0x0040,0xE8E0,0xFFF0,0xFC72,0x0470,0xEBB0,0xF8B0,0xF8B2,0xF860,0xE460,0xF422,0xF422,0xF8B0,0xF060,0xF052,0x00A2,0xFF80,0xE8A0,0xEC12,0xF422,0xFFF0,0xEBD0,0xEFF0,0xF420,0xF860,0xE83E,
0x0B82,0xF7D0,0xFFAE,0x0832,0x0FF2,0x0070,0xF04E,0x08D0,0x17F2,0x0080,0xFC9E,0x0FC0,0x10D2,0x04D0,0xF0AE,0x0C70,0x1042,0x0430,0xF0DE,0x2070,0x1062,0x10B0,0xFB9E,0x1890,0x1422,0x0840,0x041E,0x1FD0,0x1802,0x1020,0x047E,0x2020,
0xF7AE,0x0F52,0x1BA0,0xFC00,0x07BE,0x0C72,0x17E0,0x0020,0x03AE,0x0C60,0x1860,0xFBC0,0x0460,0x0CD2,0x0C50,0xF820,0x0790,0x0812,0x0C40,0x006E,0xFC70,0x1452,0x0BA0,0xFC50,0xF880,0x13A2,0x1400,0x077E,0xF830,0x0FD0,0x0C50,0x07C0,
0xDF80,0xE350,0xDF9E,0xF7E0,0xF3A0,0xE860,0xDFEE,0xFC20,0xF390,0xEC40,0xE85E,0xFFC0,0xF860,0xF0EE,0xDC5E,0x0030,0xFFA2,0xF010,0xE05E,0x0C50,0xF882,0x044E,0xE3DE,0x0C60,0xF892,0x03C0,0xF00E,0x1760,0x0012,0x03C0,0xF04E,0x1B90,
0xE370,0xFF82,0x03C0,0xEFD0,0xEBCE,0x0012,0xFFE0,0xE880,0xEF8E,0xFC10,0x04A0,0xE7DE,0xF0AE,0xFCF0,0xF8B0,0xEC5E,0xEC30,0xF852,0xF8C0,0xF48E,0xECB0,0x04D0,0xFF80,0xF0A0,0xF030,0x0020,0x07F0,0xF76E,0xF3A0,0x03D0,0x03D0,0xF7DE,
0xEB42,0xEB50,0xDFAE,0x03A0,0xF3B0,0xF7F0,0xE3DE,0xFC70,0xFF50,0xFBEE,0xEC7E,0x07A0,0x0080,0xFCE0,0xE0BE,0x0C30,0xFC30,0xFC6E,0xE4CE,0x1490,0x00A2,0x0CD0,0xF39E,0x1480,0x0C32,0x041E,0xFFDE,0x2730,0x1782,0x0FB0,0x03BE,0x2780,
0xE83E,0x0FA2,0x1390,0xFC00,0x07CE,0x1432,0x0800,0xFC3E,0xF41E,0x13E2,0x089E,0xFF7E,0x049E,0x10A0,0x00CE,0xF4FE,0xFC80,0x08A0,0x0CDE,0x04FE,0xF930,0x1D00,0x0420,0x00EE,0xF4AE,0x1040,0x042E,0x07AE,0xF7FE,0x1F70,0x004E,0x179E,
0xE010,0xF37E,0xE78E,0x07D0,0x0390,0x03FE,0xE7EE,0x0800,0xFBD0,0x07CE,0xEC8C,0x0B5E,0x0C70,0x048E,0xE8AC,0x08CE,0x0460,0x049E,0xF0BC,0x20D0,0x0920,0x14FE,0xF02C,0x20B0,0x1070,0x101E,0xFC1C,0x2780,0x1BD0,0x274E,0x003E,0x377E
};
int[] FRMDatas = {
0xFFA1,0xFF9C,0xFFA5,0xFF9B,0xFF9F,0xFF9B,0xFFA4,0xFF99,0xFFA3,0xFF9B,0xFFAC,0xFFB3,0xFFD9,0xFFD2,0xFFDD,0xFFCE,0xFFDC,0xFFCE,0xFFD5,0xFFBB,0xFFBD,0xFFB0,0xFFBB,0xFFAA,0xFFB6,0xFFA6,0xFFB6,0xFFA3,0xFFAF,0xFFA0,0xFFB0,0xFF94,
0xFF94,0xFF94,0xFF91,0xFF94,0xFF92,0xFF91,0xFF90,0xFF92,0xFF98,0xFF92,0xFFA0,0xFFA5,0xFFCD,0xFFC9,0xFFCC,0xFFC9,0xFFD3,0xFFC9,0xFFC6,0xFFBB,0xFFB5,0xFFA9,0xFFAB,0xFFA5,0xFFAE,0xFFA2,0xFFA6,0xFFA0,0xFFA4,0xFF9C,0xFF9F,0xFF90,
0xFF9F,0xFF9A,0xFFA2,0xFF9A,0xFFA0,0xFF9B,0xFFA4,0xFF9C,0xFFA9,0xFFA7,0xFFC8,0xFFCB,0xFFD8,0xFFD0,0xFFDE,0xFFD1,0xFFE0,0xFFD4,0xFFDD,0xFFCD,0xFFC5,0xFFB4,0xFFBF,0xFFB0,0xFFC5,0xFFAF,0xFFBC,0xFFA2,0xFFAA,0xFF97,0xFFA8,0xFF8F,
0xFF92,0xFF90,0xFF8F,0xFF93,0xFF96,0xFF93,0xFF95,0xFF97,0xFFA6,0xFFA6,0xFFC6,0xFFC6,0xFFCC,0xFFC8,0xFFCD,0xFFCC,0xFFD6,0xFFCE,0xFFD0,0xFFCA,0xFFC1,0xFFB0,0xFFB0,0xFFAC,0xFFBB,0xFFAD,0xFFA1,0xFF96,0xFF95,0xFF8B,0xFF92,0xFF88,
0xFFA1,0xFF9B,0xFFA3,0xFF9B,0xFFA7,0xFFA5,0xFFB6,0xFFB6,0xFFD2,0xFFCF,0xFFDA,0xFFD0,0xFFDC,0xFFD3,0xFFDF,0xFFD3,0xFFE3,0xFFD7,0xFFE2,0xFFD3,0xFFDE,0xFFC5,0xFFCB,0xFFBC,0xFFBF,0xFF9D,0xFF9F,0xFF8A,0xFF9A,0xFF8E,0xFFA5,0xFF8D,
0xFF93,0xFF90,0xFF90,0xFF94,0xFFA1,0xFFA0,0xFFBD,0xFFBD,0xFFCB,0xFFC6,0xFFC8,0xFFC8,0xFFD0,0xFFCA,0xFFCE,0xFFCC,0xFFD6,0xFFCE,0xFFCE,0xFFCB,0xFFD3,0xFFC7,0xFFBB,0xFFB7,0xFFA2,0xFF8F,0xFF8A,0xFF84,0xFF8D,0xFF86,0xFF95,0xFF87,
0xFFA2,0xFFA1,0xFFAB,0xFFA9,0xFFC0,0xFFCA,0xFFD4,0xFFC8,0xFFCB,0xFFBD,0xFFD7,0xFFD1,0xFFDA,0xFFD4,0xFFE2,0xFFD5,0xFFE0,0xFFD3,0xFFDF,0xFFD1,0xFFDE,0xFFD0,0xFFC5,0xFF9F,0xFF9F,0xFF8D,0xFF9D,0xFF89,0xFF99,0xFF8D,0xFFA6,0xFF8E,
0xFF97,0xFF96,0xFFA2,0xFFAA,0xFFBE,0xFFBF,0xFFB9,0xFFB9,0xFFA2,0xFF9E,0xFFC6,0xFFC7,0xFFCE,0xFFC9,0xFFCC,0xFFCA,0xFFD1,0xFFC8,0xFFCA,0xFFC9,0xFFCE,0xFFC1,0xFF98,0xFF8F,0xFF92,0xFF84,0xFF89,0xFF83,0xFF8D,0xFF84,0xFF94,0xFF87,
0xFFA4,0xFFAD,0xFFC9,0xFFC1,0xFFC2,0xFFAE,0xFFAC,0xFFA8,0xFFC6,0xFFD0,0xFFDD,0xFFD0,0xFFDA,0xFFD6,0xFFE1,0xFFD3,0xFFDD,0xFFCF,0xFFDC,0xFFCD,0xFFC9,0xFFA7,0xFF9C,0xFF8D,0xFF9D,0xFF8C,0xFF9B,0xFF8A,0xFF99,0xFF8A,0xFFA3,0xFF8E,
0xFF97,0xFF98,0xFFAE,0xFFB0,0xFF98,0xFF93,0xFF9F,0xFFA8,0xFFC8,0xFFC4,0xFFC0,0xFFBB,0xFFC4,0xFFC7,0xFFCA,0xFFC8,0xFFCD,0xFFC6,0xFFC5,0xFFC0,0xFFA3,0xFF90,0xFF87,0xFF86,0xFF91,0xFF82,0xFF87,0xFF82,0xFF8D,0xFF81,0xFF8E,0xFF85,
0xFFA2,0xFF9D,0xFFA5,0xFF9A,0xFFA6,0xFFAA,0xFFCE,0xFFCE,0xFFD5,0xFFC4,0xFFB0,0xFFA0,0xFFC9,0xFFD4,0xFFD4,0xFFC2,0xFFDD,0xFFD0,0xFFCE,0xFFAC,0xFF9D,0xFF8F,0xFF9B,0xFF8C,0xFF9A,0xFF8D,0xFF98,0xFF89,0xFF94,0xFF8B,0xFF9E,0xFF8B,
0xFF8E,0xFF8A,0xFF8C,0xFF8D,0xFFA4,0xFFA5,0xFFBF,0xFFC1,0xFFB0,0xFFA5,0xFF91,0xFF92,0xFFCB,0xFFC6,0xFFA6,0xFFA2,0xFFCD,0xFFC3,0xFFA3,0xFF94,0xFF8D,0xFF84,0xFF85,0xFF83,0xFF8E,0xFF81,0xFF85,0xFF7F,0xFF87,0xFF80,0xFF8C,0xFF80,
0xFF99,0xFF99,0xFFA4,0xFFAB,0xFFC8,0xFFCB,0xFFC8,0xFFA6,0xFF9C,0xFF98,0xFFAA,0xFFC7,0xFFD6,0xFFC3,0xFFB1,0xFFB6,0xFFD9,0xFFC2,0xFFA6,0xFF8F,0xFF99,0xFF8F,0xFF99,0xFF8A,0xFF99,0xFF8D,0xFF99,0xFF89,0xFF94,0xFF89,0xFF9C,0xFF88,
0xFF87,0xFF86,0xFF93,0xFF9B,0xFFB8,0xFFB7,0xFF8D,0xFF8C,0xFF89,0xFF88,0xFFB2,0xFFB6,0xFFBA,0xFFB3,0xFF9B,0xFFAA,0xFFC7,0xFFB5,0xFF87,0xFF84,0xFF89,0xFF81,0xFF83,0xFF80,0xFF8B,0xFF82,0xFF86,0xFF80,0xFF87,0xFF7D,0xFF88,0xFF7D,
0xFF97,0xFF9A,0xFF9F,0xFFA6,0xFFAC,0xFF9C,0xFF9A,0xFF93,0xFF99,0xFFAA,0xFFCE,0xFFC8,0xFFB5,0xFF98,0xFFB5,0xFFC8,0xFFCD,0xFFA6,0xFF9E,0xFF8E,0xFF99,0xFF8F,0xFF98,0xFF8B,0xFF9B,0xFF90,0xFF99,0xFF8A,0xFF97,0xFF8B,0xFF9A,0xFF87,
0xFF83,0xFF85,0xFF82,0xFF88,0xFF86,0xFF86,0xFF7F,0xFF83,0xFF95,0xFF93,0xFFB8,0xFFBA,0xFF8B,0xFF88,0xFFB0,0xFFB9,0xFFA7,0xFF92,0xFF86,0xFF82,0xFF89,0xFF81,0xFF81,0xFF80,0xFF90,0xFF83,0xFF83,0xFF80,0xFF88,0xFF7E,0xFF85,0xFF7C,
0xFF9A,0xFF95,0xFF9A,0xFF93,0xFF98,0xFF94,0xFF99,0xFF94,0xFFAA,0xFFC7,0xFFC6,0xFF98,0xFF99,0xFF9D,0xFFCD,0xFFC6,0xFFA8,0xFF91,0xFF99,0xFF8E,0xFF98,0xFF8D,0xFF98,0xFF89,0xFF98,0xFF8C,0xFF94,0xFF87,0xFF93,0xFF87,0xFF97,0xFF84,
0xFF84,0xFF80,0xFF7E,0xFF81,0xFF81,0xFF80,0xFF7E,0xFF80,0xFFA7,0xFFA0,0xFF88,0xFF89,0xFF89,0xFF8C,0xFFB8,0xFFB1,0xFF8A,0xFF80,0xFF7F,0xFF80,0xFF88,0xFF7F,0xFF81,0xFF7D,0xFF8B,0xFF7F,0xFF7F,0xFF7A,0xFF84,0xFF79,0xFF82,0xFF78,
0xFF90,0xFF94,0xFF95,0xFF90,0xFF92,0xFF90,0xFF96,0xFF91,0xFF96,0xFF92,0xFF98,0xFF8C,0xFF97,0xFFA1,0xFFB3,0xFF9C,0xFF99,0xFF8F,0xFF98,0xFF8C,0xFF97,0xFF90,0xFF9C,0xFF94,0xFFA0,0xFF97,0xFF9B,0xFF89,0xFF90,0xFF87,0xFF92,0xFF84,
0xFF7A,0xFF7E,0xFF77,0xFF7C,0xFF7D,0xFF7C,0xFF78,0xFF7D,0xFF7F,0xFF7C,0xFF7B,0xFF7D,0xFF84,0xFF81,0xFF86,0xFF84,0xFF85,0xFF7D,0xFF7D,0xFF7D,0xFF85,0xFF81,0xFF83,0xFF86,0xFF90,0xFF8A,0xFF84,0xFF7F,0xFF80,0xFF78,0xFF7D,0xFF76,
0xFF8E,0xFF90,0xFF8F,0xFF8D,0xFF8C,0xFF8F,0xFF91,0xFF8A,0xFF8F,0xFF8D,0xFF92,0xFF8A,0xFF92,0xFF90,0xFF97,0xFF8E,0xFF95,0xFF8C,0xFF96,0xFF8C,0xFF98,0xFF93,0xFF9A,0xFF8F,0xFF9E,0xFF94,0xFF9B,0xFF8B,0xFF91,0xFF85,0xFF91,0xFF81,
0xFF75,0xFF77,0xFF70,0xFF79,0xFF76,0xFF78,0xFF73,0xFF75,0xFF7A,0xFF77,0xFF74,0xFF78,0xFF7E,0xFF7B,0xFF7A,0xFF7D,0xFF81,0xFF7A,0xFF7B,0xFF7C,0xFF86,0xFF7F,0xFF80,0xFF7F,0xFF8C,0xFF83,0xFF84,0xFF80,0xFF80,0xFF74,0xFF7A,0xFF72,
0xFF89,0xFF8E,0xFF8E,0xFF8C,0xFF8C,0xFF8D,0xFF8D,0xFF89,0xFF8B,0xFF8B,0xFF8D,0xFF89,0xFF90,0xFF8D,0xFF91,0xFF8A,0xFF9A,0xFF96,0xFF9B,0xFF8B,0xFF96,0xFF92,0xFF9A,0xFF92,0xFF9E,0xFF99,0xFF9C,0xFF91,0xFF8E,0xFF88,0xFF89,0xFF80,
0xFF65,0xFF6B,0xFF65,0xFF6B,0xFF6B,0xFF6C,0xFF64,0xFF6B,0xFF6C,0xFF6B,0xFF65,0xFF6A,0xFF6F,0xFF6D,0xFF6C,0xFF71,0xFF81,0xFF7C,0xFF72,0xFF70,0xFF79,0xFF74,0xFF77,0xFF78,0xFF85,0xFF7E,0xFF7D,0xFF7A,0xFF75,0xFF6C,0xFF69,0xFF65,
0x4D71,0x18AE,0x7FFF,0x18AE,0x7FFF,0x18AD,0x7FFF,0x18AD,0xFFA9,0xCF85,0x17A8,0xD816,0x0000,0x000C,0x0003,0xFFFE,0x195E,0x03FF,0x0269,0x7FFF,0x195D,0x03FF,0x0269,0x7FFF,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
0x062B,0x7FFF,0x18AE,0x7FFF,0x18AE,0x7FFF,0x18AD,0x7FFF,0xFFAA,0xF521,0xD0F4,0xD738,0x000D,0x0005,0xFFFE,0xFFFF,0x00F5,0x004D,0x275D,0x0048,0x00F5,0x004D,0x275D,0x0048,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
0x1A01,0x0000
};
	MLX90640_Funs myFuns;
	myFuns=new MLX90640_Funs();
	double Vdd=0;
	double Ta,Tr;
	double Temp[]=new double[768];

	//使用EEM数据计算（初始化）myFuns.myPars，并依次打印出结果
	myFuns.MLX90640_ExtractParameters(EEMDatas);
	System.out.println( "kVdd="+myFuns.myPars.kVdd);
	System.out.println( "vdd25="+myFuns.myPars.vdd25);
	System.out.println( "KvPTAT="+myFuns.myPars.KvPTAT);
	System.out.println( "KtPTAT="+myFuns.myPars.KtPTAT);
	System.out.println( "vPTAT25="+myFuns.myPars.vPTAT25);
	System.out.println( "alphaPTAT="+myFuns.myPars.alphaPTAT);
	System.out.println( "gainEE="+myFuns.myPars.gainEE);//myPars.gainEE = gainEE;
	System.out.println( "tgc="+myFuns.myPars.tgc);
	System.out.println( "resolutionEE="+myFuns.myPars.resolutionEE);
	System.out.println( "KsTa="+myFuns.myPars.KsTa);
	for (int i=0;i<4;i++)
	{
	   System.out.println( "ksTo["+i+"]="+myFuns.myPars.ksTo[i]);
	}
	for (int i=0;i<768;i++)
	{
	   System.out.println( "alpha["+i+"]="+myFuns.myPars.alpha[i]);
	}
	for (int i=0;i<768;i++)
	{
	   System.out.println( "offset["+i+"]="+myFuns.myPars.offset[i]);
	}
	for (int i=0;i<768;i++)
	{
	   System.out.println( "kta["+i+"]="+myFuns.myPars.kta[i]);
	}
	for (int i=0;i<768;i++)
	{
	   System.out.println( "kv["+i+"]="+myFuns.myPars.kv[i]);
	}
	System.out.println( "cpKv="+myFuns.myPars.cpKv);
	System.out.println( "cpKta="+myFuns.myPars.cpKta);
	System.out.println( "cpAlpha[0]="+myFuns.myPars.cpAlpha[0]);
	System.out.println( "cpAlpha[1]="+myFuns.myPars.cpAlpha[1]);
	System.out.println( "cpOffset[0]="+myFuns.myPars.cpOffset[0]);
	System.out.println( "cpOffset[1]="+myFuns.myPars.cpOffset[1]);
	System.out.println( "calibrationModeEE="+myFuns.myPars.calibrationModeEE);
	System.out.println( "ilChessC[0]="+myFuns.myPars.ilChessC[0]);
	System.out.println( "ilChessC[1]="+myFuns.myPars.ilChessC[1]);
	System.out.println( "ilChessC[2]="+myFuns.myPars.ilChessC[2]);

	//使用FRM数据和myFuns.myPars计算温度，并依次打印出结果
	Vdd=myFuns.MLX90640_GetVdd(FRMDatas);
	System.out.println( "Vdd="+Vdd);
	Ta=myFuns.MLX90640_GetTa(FRMDatas);
	System.out.println( "Ta="+Ta);
	Tr=Ta-8.0;
	System.out.println( "Tr="+Tr);
	myFuns.MLX90640_CalculateTo(FRMDatas,0.95,Tr);
	//MLX90640_CalculateTo函数每次仅计算一半像素的温度，计算哪一半由FRMDatas[833]决定，模块返回的数据包，此值会为0/1交错出现，即：相邻两包数据计算完成后才是完整的768个温度数据
	for (int i=0;i<768;i++)
	{
	   System.out.println( "Mlx90640Tos["+i+"]="+myFuns.mlx90640Tos[i]);
	}
   }
}