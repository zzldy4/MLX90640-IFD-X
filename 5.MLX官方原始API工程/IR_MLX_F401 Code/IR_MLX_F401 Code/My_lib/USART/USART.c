/****************************************
��������
https://jmelectron.taobao.com/shop/view_shop.htm?spm=a1z0e.1.10010.6.VsSRMs
****************************************/

#include "USART.h"
#include "stm32f4xx_hal.h"
#include "MyType.h"


u8 UsartBuff[SEND_BUF_SIZE];//���ڴ���ͼ�񻺴�

UART_HandleTypeDef UART1_Handler; //UART���
DMA_HandleTypeDef  UART1TxDMA_Handler;      //DMA���

//��ʼ��IO ����1 
//bound:������
void uart_init(u32 bound)
{	
	HAL_UART_DeInit(&UART1_Handler);
	
	//UART ��ʼ������
	UART1_Handler.Instance=USART1;					    //USART1
	UART1_Handler.Init.BaudRate=bound;				    //������
	UART1_Handler.Init.WordLength=UART_WORDLENGTH_8B;   //�ֳ�Ϊ8λ���ݸ�ʽ
	UART1_Handler.Init.StopBits=UART_STOPBITS_1;	    //һ��ֹͣλ
	UART1_Handler.Init.Parity=UART_PARITY_NONE;		    //����żУ��λ
	UART1_Handler.Init.HwFlowCtl=UART_HWCONTROL_NONE;   //��Ӳ������
	UART1_Handler.Init.Mode=UART_MODE_TX_RX;		    //�շ�ģʽ
	HAL_UART_Init(&UART1_Handler);					    //HAL_UART_Init()��ʹ��UART1

	MYDMA_Config(DMA2_Stream7,DMA_CHANNEL_4);//��ʼ��DMA
	HAL_DMA_Start(&UART1TxDMA_Handler,(u32)UsartBuff,(u32)&USART1->DR,SEND_BUF_SIZE);	
}


//DMAx�ĸ�ͨ������
//����Ĵ�����ʽ�ǹ̶���,���Ҫ���ݲ�ͬ��������޸�
//�Ӵ洢��->����ģʽ/8λ���ݿ��/�洢������ģʽ
//DMA_Streamx:DMA������,DMA1_Stream0~7/DMA2_Stream0~7
//chx:DMAͨ��ѡ��,@ref DMA_channel DMA_CHANNEL_0~DMA_CHANNEL_7
void MYDMA_Config(DMA_Stream_TypeDef *DMA_Streamx,u32 chx)
{ 
    __HAL_RCC_DMA2_CLK_ENABLE();//DMA1ʱ��ʹ�� 
  
    __HAL_LINKDMA(&UART1_Handler,hdmatx,UART1TxDMA_Handler);    //��DMA��USART1��ϵ����(����DMA)
    
    //Tx DMA����
    UART1TxDMA_Handler.Instance=DMA_Streamx;                            //������ѡ��
    UART1TxDMA_Handler.Init.Channel=chx;                                //ͨ��ѡ��
    UART1TxDMA_Handler.Init.Direction=DMA_MEMORY_TO_PERIPH;             //�洢��������
    UART1TxDMA_Handler.Init.PeriphInc=DMA_PINC_DISABLE;                 //���������ģʽ
    UART1TxDMA_Handler.Init.MemInc=DMA_MINC_ENABLE;                     //�洢������ģʽ
    UART1TxDMA_Handler.Init.PeriphDataAlignment=DMA_PDATAALIGN_BYTE;    //�������ݳ���:8λ
    UART1TxDMA_Handler.Init.MemDataAlignment=DMA_MDATAALIGN_BYTE;       //�洢�����ݳ���:8λ
    UART1TxDMA_Handler.Init.Mode=DMA_CIRCULAR;                            //��������ģʽ
    UART1TxDMA_Handler.Init.Priority=DMA_PRIORITY_MEDIUM;               //�е����ȼ�
    UART1TxDMA_Handler.Init.FIFOMode=DMA_FIFOMODE_DISABLE;              
    UART1TxDMA_Handler.Init.FIFOThreshold=DMA_FIFO_THRESHOLD_FULL;      
    UART1TxDMA_Handler.Init.MemBurst=DMA_MBURST_SINGLE;                 //�洢��ͻ�����δ���
    UART1TxDMA_Handler.Init.PeriphBurst=DMA_PBURST_SINGLE;              //����ͻ�����δ���
    
    HAL_DMA_DeInit(&UART1TxDMA_Handler);  
		
    HAL_DMA_Init(&UART1TxDMA_Handler);
} 

