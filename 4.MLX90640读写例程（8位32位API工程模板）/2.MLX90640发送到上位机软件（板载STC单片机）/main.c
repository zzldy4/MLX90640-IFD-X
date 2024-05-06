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
void UART1SendData(unsigned char dat)
{
	SBUF=dat;
	while (!TI);
	TI=0;
}
void UART1SendString(char *s)
{
	while (*s)
	{
		UART1SendData(*s++);	
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
	UINT16 i;
	UINT16 statusRegister,controlRegister1;
   	
	P_SW2 |= 0x80;//特殊功能寄存器可写控制位

	//LED指示灯初始化
	P5M1&=~0x30;//P5.4/5(LED_STA/LED_RUN)
    P5M0|=0x30;	
	LED_STA=1;
	LED_RUN=1;

	UART1Init();//UART1=230400bps,N,8,1
	UART2Init();	
	
	IIC_Init(0);	//980kHz,0:980kHz;1:700kHz;2:500kHz;3:450kHz;4:390kHz;5:345kHz...63:41kHz
	I2CCFG|=0x80;	//使能硬件IIC接口	

	//初始化MLX90640
	UART1SendString("MLX90640 Init...");
	Delay_ms(20);
	MLX90640_I2CInit();
	Delay_ms(50);
	UART1SendString("OK\r\n");	

	Delay_ms(200);Delay_ms(200);Delay_ms(200);Delay_ms(200);Delay_ms(200);
	Delay_ms(200);Delay_ms(200);Delay_ms(200);Delay_ms(200);Delay_ms(200);
	Delay_ms(200);Delay_ms(200);Delay_ms(200);Delay_ms(200);Delay_ms(200);
	Delay_ms(200);Delay_ms(200);Delay_ms(200);Delay_ms(200);Delay_ms(200);
	Delay_ms(200);Delay_ms(200);Delay_ms(200);Delay_ms(200);Delay_ms(200);
	
	//输出MLX90640校准参数(MLX90640内部存储器的地址0x2400~0x273F)	
	MLX90640_I2CRead(0x2400, 832, Datas_n16);
	UART1SendString("MLX_EEM");
	for (i=0;i<832;i++)
	{		
		UART1SendData(Datas_n16[i]>>8);
		UART1SendData(Datas_n16[i]&0x00FF);
	}
	Delay_ms(100);	
   	
	while (1)
	{
		Delay_ms(10);
		MLX90640_I2CRead(0x800D, 1, &controlRegister1);
		MLX90640_I2CRead(0x8000, 1, &statusRegister);
		if (statusRegister&0x0008)//有测量完成的Frame
		{
			MLX90640_I2CWrite(0x8000, statusRegister&(~0x0018));

			MLX90640_I2CRead(0x0400, 832, Datas_n16);

			LED_RUN=1;
			UART1SendString("MLX_FRM");
			for (i=0;i<832;i++)
			{
				UART1SendData(Datas_n16[i]>>8);
				UART1SendData(Datas_n16[i]&0x00FF);	
			}		

			//输出控制寄存器1和状态寄存器中的子页码
			UART1SendData(controlRegister1>>8);
			UART1SendData(controlRegister1&0x00FF);
			UART1SendData(0x00);
			UART1SendData(statusRegister&0x0001);

			LED_RUN=0;
		}	
	}
}
