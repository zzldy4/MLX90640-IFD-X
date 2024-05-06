#ifndef __IIC_H
#define __IIC_H
#include "cpu.h"


sbit SDA=P3^3;
sbit SCL=P3^2;
/********************************************************************
**                          常数及宏定义
********************************************************************/
#define		I2C_CMD_NULL	0x00	//待机，无动作
#define		I2C_CMD_START	0x01	//发送开始信号（强制发送，无论忙不忙，即忽略MSST.7的忙状态，自此信号开始MSST.7一直为1，表示忙）
#define		I2C_CMD_SBYTE	0x02	//发送TXD寄存器中的1字节数据
#define		I2C_CMD_RACK	0x03	//接收ACK信号，保存到MSST.1位
#define		I2C_CMD_RBYTE	0x04	//接收1字节数据到RXD寄存器
#define		I2C_CMD_SACK	0x05	//将MSST.0的信号发送ACK信号
#define		I2C_CMD_STOP	0x06	//发送停止信号（停止信号发送完成后，才会自动清0）
//以下指令只对8F2K64Sx系列C/D版、8A8K64SxA12的E/F版、、、
#define		I2C_CMD_START_SBYTE_RACK	0x09	//发送开始信号+发送1字节数据+接收ACK信号
#define		I2C_CMD_SBYTE_RACK	0x0A	//发送1字节数据+接收ACK信号
#define		I2C_CMD_RBYTE_ACK	0x0B	//接收1字节数据+发送ACK信号0（应答，不需要专门写MSST.0）
#define		I2C_CMD_RBYTE_NACK	0x0C	//接收1字节数据+发送ACK信号1（非应答，不需要专门写MSST.0）

/********************************************************************
**                         函数原型声明
********************************************************************/
void IIC_Init(UINT16 baud);
void IIC_Unlock(void);
unsigned char IIC_Wait(void);
void IIC_Start(void);
void IIC_SendData(char dat);
unsigned char IIC_RecvACK(void);
char IIC_RecvData(void);
void IIC_SendACK(void);
void IIC_SendNAK(void);
void IIC_Stop(void);
void IIC_Stop2(void);
#endif

/********************************************************************
**                            End Of File
********************************************************************/