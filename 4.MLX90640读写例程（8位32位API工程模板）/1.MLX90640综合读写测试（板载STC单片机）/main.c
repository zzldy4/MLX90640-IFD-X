#include <STC8FXX.h>
#include "iic.h"
#include "MLX90640.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include<ctype.h>

sfr IRCCR=0x9F;

sbit LED_STA=P5^4;//P5.4(LED_STA)
sbit LED_RUN=P5^5;//P5.5(LED_RUN)

void Delay_us(BYTE us)
{
	while (us--);
}
void Delay_ms(UINT16 ms)
{
	unsigned char i;   

    while(ms--)
	{
		for (i=0;i<10;i++)
			Delay_us(100);
	}
}
void UART1Init(void)
{
	P_SW1 &=~ 0xC0;                               //RXD/P3.0, TXD/P3.1
	
	SCON = 0x50;
    TMOD &=~ 0xF0;
    TL1 = 0xF4;
    TH1 = 0xFF;
    TR1 = 1;
    AUXR |= 0x40;		

	ES = 1;
	EA = 1;	
}
void UART1SendString(char *s)
{
	while (*s)
	{
		SBUF=*s++;
		while (!TI);
		TI=0;
	} 
}
void UART2Init(void)
{	
	unsigned char temp=TMOD;
	P_SW2 |= 0x80;
	P_SW2 &= ~0x01;
	
	S2CON = 0x50;	
    T2L = 0xF4;
    T2H = 0xFF;
    AUXR |= 0x14;
	IE2 |= 0x01;
	EA = 1;
	TMOD=temp;
}

void main(void)
{
	UINT16 Datas_n16[832];
	char tempStr[50];
	UINT16 i;
	UINT16 statusRegister,controlRegister1;
   	
	P_SW2 |= 0x80;//���⹦�ܼĴ�����д����λ

	//LEDָʾ�Ƴ�ʼ��
	P5M1&=~0x30;//P5.4/5(LED_STA/LED_RUN)
    P5M0|=0x30;	
	LED_STA=1;
	LED_RUN=1;

	UART1Init();//UART1=230400bps,N,8,1
	UART2Init();	
	
	IIC_Init(0);	//980kHz,0:980kHz;1:700kHz;2:500kHz;3:450kHz;4:390kHz;5:345kHz...63:41kHz
	I2CCFG|=0x80;	//ʹ��Ӳ��IIC�ӿ�	

	//��ʼ��MLX90640
	UART1SendString("MLX90640 Init...");
	Delay_ms(20);
	MLX90640_I2CInit();
	Delay_ms(50);
	UART1SendString("OK\r\n");	

	//��ȡ�Ĵ�������UART1���(MLX90640�ڲ��洢���ĵ�ַ0x8000~0x8016)
	UART1SendString("MLX90640 reading registers...\r\n");
	MLX90640_I2CRead(0x8000, 23, Datas_n16);
	for (i=0;i<23;i++)
	{
		sprintf(tempStr,"Reg[0x%04X]=%04X\t",0x8000+i,Datas_n16[i]);
		UART1SendString(tempStr);
	}
	UART1SendString("\r\n");

	//����MLX90640����
	MLX90640_SetResolution(2);	//�����ֱ���18λ(0~3��Ӧ16,17,18,19λ�ֱ���)
	MLX90640_SetRefreshRate(0);	//��������1Hz(0~7��Ӧ0.5,1,2,4,8,16,32,64Hz)
	MLX90640_SetArrangeMode(1);	//֡ģʽ������ģʽ�����ؽ���ģʽ��,0:TV�н���;1:�������ؽ���;

	//�ٴζ�ȡ�Ĵ�������UART1���(MLX90640�ڲ��洢���ĵ�ַ0x8000~0x8016)
	UART1SendString("MLX90640 reading registers...\r\n");
	MLX90640_I2CRead(0x8000, 23, Datas_n16);
	for (i=0;i<23;i++)
	{
		sprintf(tempStr,"Reg[0x%04X]=%04X\t",0x8000+i,Datas_n16[i]);
		UART1SendString(tempStr);
	}
	UART1SendString("\r\n");

	Delay_ms(200);
	
	//���MLX90640У׼����(MLX90640�ڲ��洢���ĵ�ַ0x2400~0x273F)	
	UART1SendString("MLX90640 reading EEPROM...\r\n");
	MLX90640_I2CRead(0x2400, 832, Datas_n16);
	for (i=0;i<832;i++)
	{
		if (i % 32==0)
		{
			sprintf(tempStr,"\r\nEEM[0x%04X] ",0x2400+i);
			UART1SendString(tempStr);
		}
		sprintf(tempStr,"%04X ",Datas_n16[i]);
		UART1SendString(tempStr);
	}
	UART1SendString("\r\n");
	Delay_ms(50);	
   	
	while (1)
	{
		Delay_ms(20);
		MLX90640_I2CRead(0x800D, 1, &controlRegister1);
		MLX90640_I2CRead(0x8000, 1, &statusRegister);
		if (statusRegister&0x0008)//�в�����ɵ�Frame
		{
			LED_RUN=1;

			MLX90640_I2CWrite(0x8000, statusRegister&(~0x0018));

			MLX90640_I2CRead(0x0400, 832, Datas_n16);
			for (i=0;i<832;i++)
			{
				if (i % 32==0)
				{
					sprintf(tempStr,"\r\nFRM[0x%04X] ",0x0400+i);
					UART1SendString(tempStr);
				}
				sprintf(tempStr,"%04X ",Datas_n16[i]);
				UART1SendString(tempStr);
			}
			UART1SendString("\r\n");

			//������ƼĴ���1��״̬�Ĵ����е���ҳ��
			sprintf(tempStr,"CONTROL1=%04X\r\n",controlRegister1);
			UART1SendString(tempStr);
			sprintf(tempStr,"STATUS=%04X\r\n",statusRegister&0x0001);
			UART1SendString(tempStr);

			LED_RUN=0;
		}	
	}
}
