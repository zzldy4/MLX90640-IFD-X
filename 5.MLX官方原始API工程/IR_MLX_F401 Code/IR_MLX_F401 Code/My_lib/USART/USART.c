/****************************************
今明电子
https://jmelectron.taobao.com/shop/view_shop.htm?spm=a1z0e.1.10010.6.VsSRMs
****************************************/

#include "USART.h"
#include "stm32f4xx_hal.h"
#include "MyType.h"


u8 UsartBuff[SEND_BUF_SIZE];//串口传输图像缓存

UART_HandleTypeDef UART1_Handler; //UART句柄
DMA_HandleTypeDef  UART1TxDMA_Handler;      //DMA句柄

//初始化IO 串口1 
//bound:波特率
void uart_init(u32 bound)
{	
	HAL_UART_DeInit(&UART1_Handler);
	
	//UART 初始化设置
	UART1_Handler.Instance=USART1;					    //USART1
	UART1_Handler.Init.BaudRate=bound;				    //波特率
	UART1_Handler.Init.WordLength=UART_WORDLENGTH_8B;   //字长为8位数据格式
	UART1_Handler.Init.StopBits=UART_STOPBITS_1;	    //一个停止位
	UART1_Handler.Init.Parity=UART_PARITY_NONE;		    //无奇偶校验位
	UART1_Handler.Init.HwFlowCtl=UART_HWCONTROL_NONE;   //无硬件流控
	UART1_Handler.Init.Mode=UART_MODE_TX_RX;		    //收发模式
	HAL_UART_Init(&UART1_Handler);					    //HAL_UART_Init()会使能UART1

	MYDMA_Config(DMA2_Stream7,DMA_CHANNEL_4);//初始化DMA
	HAL_DMA_Start(&UART1TxDMA_Handler,(u32)UsartBuff,(u32)&USART1->DR,SEND_BUF_SIZE);	
}


//DMAx的各通道配置
//这里的传输形式是固定的,这点要根据不同的情况来修改
//从存储器->外设模式/8位数据宽度/存储器增量模式
//DMA_Streamx:DMA数据流,DMA1_Stream0~7/DMA2_Stream0~7
//chx:DMA通道选择,@ref DMA_channel DMA_CHANNEL_0~DMA_CHANNEL_7
void MYDMA_Config(DMA_Stream_TypeDef *DMA_Streamx,u32 chx)
{ 
    __HAL_RCC_DMA2_CLK_ENABLE();//DMA1时钟使能 
  
    __HAL_LINKDMA(&UART1_Handler,hdmatx,UART1TxDMA_Handler);    //将DMA与USART1联系起来(发送DMA)
    
    //Tx DMA配置
    UART1TxDMA_Handler.Instance=DMA_Streamx;                            //数据流选择
    UART1TxDMA_Handler.Init.Channel=chx;                                //通道选择
    UART1TxDMA_Handler.Init.Direction=DMA_MEMORY_TO_PERIPH;             //存储器到外设
    UART1TxDMA_Handler.Init.PeriphInc=DMA_PINC_DISABLE;                 //外设非增量模式
    UART1TxDMA_Handler.Init.MemInc=DMA_MINC_ENABLE;                     //存储器增量模式
    UART1TxDMA_Handler.Init.PeriphDataAlignment=DMA_PDATAALIGN_BYTE;    //外设数据长度:8位
    UART1TxDMA_Handler.Init.MemDataAlignment=DMA_MDATAALIGN_BYTE;       //存储器数据长度:8位
    UART1TxDMA_Handler.Init.Mode=DMA_CIRCULAR;                            //外设流控模式
    UART1TxDMA_Handler.Init.Priority=DMA_PRIORITY_MEDIUM;               //中等优先级
    UART1TxDMA_Handler.Init.FIFOMode=DMA_FIFOMODE_DISABLE;              
    UART1TxDMA_Handler.Init.FIFOThreshold=DMA_FIFO_THRESHOLD_FULL;      
    UART1TxDMA_Handler.Init.MemBurst=DMA_MBURST_SINGLE;                 //存储器突发单次传输
    UART1TxDMA_Handler.Init.PeriphBurst=DMA_PBURST_SINGLE;              //外设突发单次传输
    
    HAL_DMA_DeInit(&UART1TxDMA_Handler);  
		
    HAL_DMA_Init(&UART1TxDMA_Handler);
} 

