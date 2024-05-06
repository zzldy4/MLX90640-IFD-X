#ifndef _MLX640_API_H
#define _MLX640_API_H

typedef struct
    {
        SINT16 kVdd;				//2字节		总2字节
        SINT16 vdd25;				//2字节		总4字节
        float KvPTAT;				//4字节		总8字节
        float KtPTAT;				//4字节		总12字节
        UINT16 vPTAT25;				//2字节		总14字节
        float alphaPTAT;			//4字节		总18字节
        SINT16 gainEE;				//2字节		总20字节
        float tgc;					//4字节		总24字节
        float cpKv;					//4字节		总28字节
        float cpKta;				//4字节		总32字节
        UINT8 resolutionEE;			//1字节		总33字节
        UINT8 calibrationModeEE;	//1字节		总34字节
        float KsTa;					//4字节		总38字节
        float ksTo[4];				//16字节	总54字节
        SINT16 ct[4];				//8字节		总62字节
        float alpha[768];			//3072字节	总3134字节    
        SINT16 offset[768];			//1536字节	总4670字节    
        float kta[768];				//3072字节	总7742字节    
        float kv[768];				//3072字节	总10814字节
        float cpAlpha[2];			//8字节		总10822字节
        SINT16 cpOffset[2];			//4字节		总10826字节
        float ilChessC[3];			//12字节	总10838字节 
        UINT16 brokenPixels[5];		//10字节	总10848字节
        UINT16 outlierPixels[5];	//10字节	总10858字节  
    } paramsMLX90640;

/********************************************************************
**                          常数及宏定义
********************************************************************/
UINT16 MLX90640_ExtractParameters(UINT16 *eeData, paramsMLX90640 *mlx90640);
float MLX90640_GetVdd(UINT16 *frameData, const paramsMLX90640 *params);
float MLX90640_GetTa(UINT16 *frameData, const paramsMLX90640 *params);
void MLX90640_CalculateTo(UINT16 *frameData, const paramsMLX90640 *params, float emissivity, float tr, float *result);
void MLX90640_GetImage(UINT16 *frameData, const paramsMLX90640 *params, float *result);
void MLX90640_BadPixelsCorrection(UINT16 *pixels, float *to, int mode, paramsMLX90640 *params);	
#endif
