/****************************************
��������
https://jmelectron.taobao.com/shop/view_shop.htm?spm=a1z0e.1.10010.6.VsSRMs
****************************************/

#ifndef __USART_H
#define __USART_H 

#include "MyType.h"

#define SEND_BUF_SIZE 1544	//�������ݳ���


extern u8 UsartBuff[SEND_BUF_SIZE];
extern UART_HandleTypeDef UART1_Handler; //UART���

void uart_init(u32 bound);
void MYDMA_Config(DMA_Stream_TypeDef *DMA_Streamx,u32 chx);	


#endif