#include "delay.h"
#include "stm32f4xx_hal.h"
#include "MyType.h"

u16 fac_ms;//全局变量
u8 fac_us;//全局变量

__IO u16 ntime;	
							    
void delay_ms(u16 nms)
{
   	  SysTick->LOAD = (u32)fac_ms*nms-1;//加载时间值
	  SysTick->VAL = 1;//随便写个值，清除加载寄存器的值
	  SysTick->CTRL |= BIT(0);//SysTick使能
	  while(!(SysTick->CTRL&(1<<16)));//判断是否减到0
	  SysTick->CTRL &=~BIT(0);//关闭SysTick
}

void Delay_Init(u8 SYSCLK)
{
     SysTick->CTRL &=~BIT(2);//选择外部时钟
	 SysTick->CTRL &=~BIT(1);//关闭定时器减到0后的中短请求
	 fac_us = SYSCLK/8;//计算好SysTick加载值
	 fac_ms = (u16)fac_us*1000;	 
}

		    								   
void delay_us(u32 nus)
{		
	  SysTick->LOAD = (u32)fac_us*nus-1;//加载时间值
	  SysTick->VAL = 1;//随便写个值，清除加载寄存器的值
	  SysTick->CTRL |= BIT(0);//SysTick使能
	  while(!(SysTick->CTRL&(1<<16)));//判断是否减到0
	  SysTick->CTRL &=~BIT(0);//关闭SysTick
}


void Delay(u32 count)
{
	while(count--);
}
