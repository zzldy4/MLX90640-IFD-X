#ifndef __IIC_H
#define __IIC_H
#include "cpu.h"

/********************************************************************
**                          常数及宏定义
********************************************************************/


/********************************************************************
**                         函数原型声明
********************************************************************/
void IIC_Init(void);
void IIC_Start(void);
void IIC_Stop(void);
UINT8 IIC_RecvACK(void);

void IIC_SendACK(void);
void IIC_SendNAK(void);

UINT8 IIC_RecvData(void);
void IIC_SendData(UINT8 dat);

#endif

/********************************************************************
**                            End Of File
********************************************************************/
