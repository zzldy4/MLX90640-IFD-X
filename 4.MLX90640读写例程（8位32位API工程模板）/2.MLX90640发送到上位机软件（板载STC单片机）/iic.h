#ifndef __IIC_H
#define __IIC_H
#include "cpu.h"


sbit SDA=P3^3;
sbit SCL=P3^2;
/********************************************************************
**                          �������궨��
********************************************************************/
#define		I2C_CMD_NULL	0x00	//�������޶���
#define		I2C_CMD_START	0x01	//���Ϳ�ʼ�źţ�ǿ�Ʒ��ͣ�����æ��æ��������MSST.7��æ״̬���Դ��źſ�ʼMSST.7һֱΪ1����ʾæ��
#define		I2C_CMD_SBYTE	0x02	//����TXD�Ĵ����е�1�ֽ�����
#define		I2C_CMD_RACK	0x03	//����ACK�źţ����浽MSST.1λ
#define		I2C_CMD_RBYTE	0x04	//����1�ֽ����ݵ�RXD�Ĵ���
#define		I2C_CMD_SACK	0x05	//��MSST.0���źŷ���ACK�ź�
#define		I2C_CMD_STOP	0x06	//����ֹͣ�źţ�ֹͣ�źŷ�����ɺ󣬲Ż��Զ���0��
//����ָ��ֻ��8F2K64Sxϵ��C/D�桢8A8K64SxA12��E/F�桢����
#define		I2C_CMD_START_SBYTE_RACK	0x09	//���Ϳ�ʼ�ź�+����1�ֽ�����+����ACK�ź�
#define		I2C_CMD_SBYTE_RACK	0x0A	//����1�ֽ�����+����ACK�ź�
#define		I2C_CMD_RBYTE_ACK	0x0B	//����1�ֽ�����+����ACK�ź�0��Ӧ�𣬲���Ҫר��дMSST.0��
#define		I2C_CMD_RBYTE_NACK	0x0C	//����1�ֽ�����+����ACK�ź�1����Ӧ�𣬲���Ҫר��дMSST.0��

/********************************************************************
**                         ����ԭ������
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