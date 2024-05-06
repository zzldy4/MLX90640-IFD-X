/********************************************************************
**                           ����ͷ�ļ�
********************************************************************/
#include <STC8FXX.h>
#include "cpu.h"
#include "iic.h"
#include "MLX90640.h"

void MLX90640_I2CInit()
{   
    IIC_Stop();
}
//��ָ����ַ��ȡn���֣�ÿ����ռ��2���ֽڣ�
unsigned char MLX90640_I2CRead(unsigned int startAddress, unsigned int nWordsRead, unsigned int *datas)
{
	unsigned char c1,c2;
	unsigned int i;
	unsigned char Msb,Lsb;

	Msb=(unsigned char)(startAddress>>8);
	Lsb=(unsigned char)(startAddress&0x00FF);	

	IIC_Start();                                    //������ʼ����	

	IIC_SendData(MLX_Addr);                             //�����豸��ַ+д����	
	IIC_RecvACK();
	IIC_SendData(Msb);                  //����Ҫ�����ĵ�ֵַ2�ֽ�
	IIC_RecvACK();
	IIC_SendData(Lsb);
	IIC_RecvACK();
	
	IIC_Start();                                   //������ʼ����
	IIC_SendData(MLX_Addr+1);                             //�����豸��ַ+������
	IIC_RecvACK();
	for (i=0;i<nWordsRead;i++)
	{		
		c1=IIC_RecvData();
		IIC_SendACK();
		c2=IIC_RecvData();
		if (i==(nWordsRead-1))
			IIC_SendNAK();
		else
			IIC_SendACK();

		datas[i]=c1;
		datas[i]<<=8;
		datas[i]|=c2;		
	}	
	IIC_Stop();                                     //����ֹͣ����

	return 0;
}

unsigned char MLX90640_I2CWrite(unsigned int writeAddress, unsigned int word)
{
	IIC_Start();                                    //������ʼ����
	IIC_SendData(MLX_Addr);                             //�����豸��ַ+д����
	IIC_RecvACK();
	IIC_SendData(writeAddress>>8);                  //����Ҫ�����ĵ�ֵַ2�ֽ�
	IIC_RecvACK();
	IIC_SendData(writeAddress&0x00FF);
	IIC_RecvACK();

	IIC_SendData(word>>8);
	IIC_RecvACK();
	IIC_SendData(word&0x00FF);
	IIC_RecvACK();

	IIC_Stop();

	return 0;
}

signed char MLX90640_DataReady(void)//RAM�����Ƿ����������-1��ʾδ������0��ʾFrame0������1��ʾFrame1����
{
	unsigned int statusReg[1];

	MLX90640_I2CRead(0x8000, 1, statusReg);
	if (statusReg[0]&0x0008)//�в�����ɵ�Frame
	{
		return (statusReg[0]&0x0001);
	}
	return (-1);
}

void MLX90640_SetResolution(unsigned char resolution)
{
    unsigned int controlRegister1;   
    
    MLX90640_I2CRead(0x800D, 1, &controlRegister1);

	controlRegister1&=~0x0C00;//bit11:10
	controlRegister1|=(resolution<<10);
    MLX90640_I2CWrite(0x800D, controlRegister1);
}

unsigned char MLX90640_GetCurResolution(void)
{
    unsigned int controlRegister1;
    unsigned int Ram;
    
    MLX90640_I2CRead(0x800D, 1, &controlRegister1);
    
    Ram = (controlRegister1 & 0x0C00) >> 10;
    
    return Ram; 
}

void MLX90640_SetRefreshRate(unsigned char refreshRate)
{
    unsigned int controlRegister1;   
    
    MLX90640_I2CRead(0x800D, 1, &controlRegister1);

	controlRegister1&=~0x0380;//bit9:7
	controlRegister1|=(refreshRate<<7);
    MLX90640_I2CWrite(0x800D, controlRegister1);
}

unsigned char MLX90640_GetRefreshRate(void)
{
    unsigned int controlRegister1;
    unsigned int Ram;
    
    MLX90640_I2CRead(0x800D, 1, &controlRegister1);
    
    Ram = (controlRegister1 & 0x0380) >> 7;
    
    return Ram;
}

void MLX90640_SetArrangeMode(unsigned char mode)
{
    unsigned int controlRegister1;   
    
    MLX90640_I2CRead(0x800D, 1, &controlRegister1);

	controlRegister1&=~0x1000;//bit12
	controlRegister1|=(mode<<12);
    MLX90640_I2CWrite(0x800D, controlRegister1);
}

unsigned char MLX90640_GetArrangeMode(void)
{
    unsigned int controlRegister1;
    unsigned int Ram;
    
    MLX90640_I2CRead(0x800D, 1, &controlRegister1);
    
    Ram = (controlRegister1 & 0x1000) >> 12;
    
    return Ram; 
}
