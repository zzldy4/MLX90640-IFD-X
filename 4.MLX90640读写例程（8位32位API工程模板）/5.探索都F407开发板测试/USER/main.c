#include "sys.h"
#include "delay.h"  
#include "usart.h"   
#include "led.h"
#include "lcd.h"
#include "key.h"  
#include "touch.h"
#include "MLX90640.h"
#include "MLX90640_ExFun.h"
//ALIENTEK 探索者STM32F407开发板 实验28
//触摸屏实验   --库函数版本
//技术支持：www.openedv.com
//淘宝店铺：http://eboard.taobao.com  
//广州市星翼电子科技有限公司  
//作者：正点原子 @ALIENTEK

//清空屏幕并在右上角显示"RST"
void Load_Drow_Dialog(void)
{
	LCD_Clear(WHITE);//清屏   
 	POINT_COLOR=BLUE;//设置字体为蓝色 
	LCD_ShowString(lcddev.width-24,0,200,16,16,"RST");//显示清屏区域
  	POINT_COLOR=RED;//设置画笔蓝色 
}
////////////////////////////////////////////////////////////////////////////////
//电容触摸屏专有部分
//画水平线
//x0,y0:坐标
//len:线长度
//color:颜色
void gui_draw_hline(u16 x0,u16 y0,u16 len,u16 color)
{
	if(len==0)return;
	LCD_Fill(x0,y0,x0+len-1,y0,color);	
}
//画实心圆
//x0,y0:坐标
//r:半径
//color:颜色
void gui_fill_circle(u16 x0,u16 y0,u16 r,u16 color)
{											  
	u32 i;
	u32 imax = ((u32)r*707)/1000+1;
	u32 sqmax = (u32)r*(u32)r+(u32)r/2;
	u32 x=r;
	gui_draw_hline(x0-r,y0,2*r,color);
	for (i=1;i<=imax;i++) 
	{
		if ((i*i+x*x)>sqmax)// draw lines from outside  
		{
 			if (x>imax) 
			{
				gui_draw_hline (x0-i+1,y0+x,2*(i-1),color);
				gui_draw_hline (x0-i+1,y0-x,2*(i-1),color);
			}
			x--;
		}
		// draw lines from inside (center)  
		gui_draw_hline(x0-x,y0+i,2*x,color);
		gui_draw_hline(x0-x,y0-i,2*x,color);
	}
}  
//两个数之差的绝对值 
//x1,x2：需取差值的两个数
//返回值：|x1-x2|
u16 my_abs(u16 x1,u16 x2)
{			 
	if(x1>x2)return x1-x2;
	else return x2-x1;
}  
//画一条粗线
//(x1,y1),(x2,y2):线条的起始坐标
//size：线条的粗细程度
//color：线条的颜色
void lcd_draw_bline(u16 x1, u16 y1, u16 x2, u16 y2,u8 size,u16 color)
{
	u16 t; 
	int xerr=0,yerr=0,delta_x,delta_y,distance; 
	int incx,incy,uRow,uCol; 
	if(x1<size|| x2<size||y1<size|| y2<size)return; 
	delta_x=x2-x1; //计算坐标增量 
	delta_y=y2-y1; 
	uRow=x1; 
	uCol=y1; 
	if(delta_x>0)incx=1; //设置单步方向 
	else if(delta_x==0)incx=0;//垂直线 
	else {incx=-1;delta_x=-delta_x;} 
	if(delta_y>0)incy=1; 
	else if(delta_y==0)incy=0;//水平线 
	else{incy=-1;delta_y=-delta_y;} 
	if( delta_x>delta_y)distance=delta_x; //选取基本增量坐标轴 
	else distance=delta_y; 
	for(t=0;t<=distance+1;t++ )//画线输出 
	{  
		gui_fill_circle(uRow,uCol,size,color);//画点 
		xerr+=delta_x ; 
		yerr+=delta_y ; 
		if(xerr>distance) 
		{ 
			xerr-=distance; 
			uRow+=incx; 
		} 
		if(yerr>distance) 
		{ 
			yerr-=distance; 
			uCol+=incy; 
		} 
	}  
}   
////////////////////////////////////////////////////////////////////////////////
 //5个触控点的颜色(电容触摸屏用)												 
const u16 POINT_COLOR_TBL[OTT_MAX_TOUCH]={RED,GREEN,BLUE,BROWN,GRED};  
//电阻触摸屏测试函数
void rtp_test(void)
{
	u8 key;
	u8 i=0;	  
	while(1)
	{
	 	key=KEY_Scan(0);
		tp_dev.scan(0); 		 
		if(tp_dev.sta&TP_PRES_DOWN)			//触摸屏被按下
		{	
		 	if(tp_dev.x[0]<lcddev.width&&tp_dev.y[0]<lcddev.height)
			{	
				if(tp_dev.x[0]>(lcddev.width-24)&&tp_dev.y[0]<16)Load_Drow_Dialog();//清除
				else TP_Draw_Big_Point(tp_dev.x[0],tp_dev.y[0],RED);		//画图	  			   
			}
		}else delay_ms(10);	//没有按键按下的时候 	    
		if(key==KEY0_PRES)	//KEY0按下,则执行校准程序
		{
			LCD_Clear(WHITE);	//清屏
		    TP_Adjust();  		//屏幕校准 
			TP_Save_Adjdata();	 
			Load_Drow_Dialog();
		}
		i++;
		if(i%20==0)LED0=!LED0;
	}
}
//电容触摸屏测试函数
void ctp_test(void)
{
	u8 t=0;
	u8 i=0;	  	    
 	u16 lastpos[5][2];		//最后一次的数据 
	while(1)
	{
		tp_dev.scan(0);
		for(t=0;t<OTT_MAX_TOUCH;t++)
		{
			if((tp_dev.sta)&(1<<t))
			{
				if(tp_dev.x[t]<lcddev.width&&tp_dev.y[t]<lcddev.height)
				{
					if(lastpos[t][0]==0XFFFF)
					{
						lastpos[t][0] = tp_dev.x[t];
						lastpos[t][1] = tp_dev.y[t];
					}
					lcd_draw_bline(lastpos[t][0],lastpos[t][1],tp_dev.x[t],tp_dev.y[t],2,POINT_COLOR_TBL[t]);//画线
					lastpos[t][0]=tp_dev.x[t];
					lastpos[t][1]=tp_dev.y[t];
					if(tp_dev.x[t]>(lcddev.width-24)&&tp_dev.y[t]<20)
					{
						Load_Drow_Dialog();//清除
					}
				}
			}else lastpos[t][0]=0XFFFF;
		}
		
		delay_ms(5);i++;
		if(i%20==0)LED0=!LED0;
	}	
}
#define INTER_TIME		1
UINT16 Datas_n16[834];
UINT16 statusRegister,controlRegister1;//???????????
	paramsMLX90640 mlx90640Pars;//????????????	
	float emissivity = 0.95;//????(??)??????,???0.95,???????
	float Vdd,Ta,tr;//MLX90640????????????????????
	float mlx90640To[768];//768?????,??:???
	UINT8 Frame0Ok=0,Frame1Ok=0;
UINT32 i,colIndex,rowIndex;
	UINT32 colCount,rowCount,pixCount;
#if INTER_TIME>0
	float mlxTemp_Interpolation1[12288];//?1???????		128*96 =12288
#endif
#if INTER_TIME>1
	float mlxTemp_Interpolation2[196608];//?2???????	512*384=196608
#endif
UINT8 Color[4];
unsigned short colorI16;
int main(void)
{ 
	
	char tempStr[100];
	
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);//设置系统中断优先级分组2
	delay_init(168);  //初始化延时函数
	uart_init(115200);		//初始化串口波特率为115200
	
	LED_Init();					//初始化LED 
 	LCD_Init();					//LCD初始化 
	KEY_Init(); 				//按键初始化  
	tp_dev.init();				//触摸屏初始化
 	POINT_COLOR=RED;//设置字体为红色 
	LCD_ShowString(30,50,200,16,16,"Explorer STM32F4");	
	LCD_ShowString(30,70,200,16,16,"TOUCH TEST");	
	LCD_ShowString(30,90,200,16,16,"ATOM@ALIENTEK");
	LCD_ShowString(30,110,200,16,16,"2014/5/7");
   	if(tp_dev.touchtype!=0XFF)LCD_ShowString(30,130,200,16,16,"Press KEY0 to Adjust");//电阻屏才显示
	delay_ms(200);
 	Load_Drow_Dialog();
	
	MLX90640_I2CInit();
	delay_ms(50);
	
	//??MLX90640??
	//MLX90640_SetResolution(2);	//?????18?(0~3??16,17,18,19????)
	MLX90640_SetRefreshRate(4);	//????1Hz(0~7??0.5,1,2,4,8,16,32,64Hz)
	MLX90640_SetArrangeMode(0);	//???,????(??????),0:TV???;1:??????;
	
	delay_ms(200);	
	GetEEPROM(Datas_n16);
	MLX90640_ExtractParameters(Datas_n16, &mlx90640Pars);
	
	delay_ms(50);
	while (1)
	{
		delay_ms(10);
		
		statusRegister=GetStatusReg();
		controlRegister1=GetControlReg();
		
		if (statusRegister&0x0008)//??????Frame
		{						
			//??RAM,??????????????????????
			GetRAM(Datas_n16);
			Datas_n16[832]=controlRegister1;
			Datas_n16[833]=statusRegister&0x0001;
			
			//if (Datas_n16[833]==0) 	Datas_n16[833]=1;
			//else					Datas_n16[833]=0;
			
			if (Datas_n16[833]==0) Frame0Ok=1;
			if (Datas_n16[833]==1) Frame1Ok=1;
			
			Vdd=MLX90640_GetVdd(Datas_n16, &mlx90640Pars);
			sprintf(tempStr,"Vdd=%5.2fV",Vdd);
			LCD_ShowString(30,250,200,16,16,tempStr);
			
			//????????mlx90640To??
			Ta= MLX90640_GetTa(Datas_n16, &mlx90640Pars);
			tr = Ta-8.0;
			MLX90640_CalculateTo(Datas_n16, &mlx90640Pars, emissivity, tr, mlx90640To);
			
			MLX90640_I2CWrite(0x8000, statusRegister&(~0x0008));
			//??????
			if ((Frame0Ok) && (Frame1Ok))
			{
				//MLX90640_BadPixelsCorrection(mlx90640Pars.brokenPixels, mlx90640To, 1, &mlx90640Pars);
				//MLX90640_BadPixelsCorrection(mlx90640Pars.outlierPixels, mlx90640To, 1, &mlx90640Pars);				
			}
			colCount=32;rowCount=24;pixCount=768;//32*24=768
#if INTER_TIME>0			
			//?1??????(????????4?,?:?32*24=768???128*96=12288)
			if ((Frame0Ok) && (Frame1Ok))
			{
				PolynomialInterpolation_Matrix(mlx90640To,colCount,rowCount,0,2,mlxTemp_Interpolation1);
				colCount=128;rowCount=96;pixCount=12288;//128*96=12288
			}
#endif
#if INTER_TIME>1
			//?2??????(????????4?,?:?128*96=12288???512*384=196608)
			if ((Frame0Ok) && (Frame1Ok))
			{
				PolynomialInterpolation_Matrix(mlxTemp_Interpolation1,colCount,rowCount,1,2,mlxTemp_Interpolation2);
				colCount=512;rowCount=384;pixCount=196608;//512*384=196608
			}
#endif			
			
			if ((Frame0Ok) && (Frame1Ok))
			{
				for (i=0;i<pixCount;i++)
				{
#if INTER_TIME==0
					TemperatureToRGB(mlx90640To[i],0.0,35.0,Color);
#elif INTER_TIME==1
					TemperatureToRGB(mlxTemp_Interpolation1[i],0.0,35.0,Color);
#elif INTER_TIME==2
					TemperatureToRGB(mlxTemp_Interpolation2[i],0.0,35.0,Color);
#endif
					//??????????
					rowIndex=i/colCount;
					colIndex=i%colCount;
					rowIndex=rowIndex;colIndex=colIndex;//?????,????????????rowIndex?colIndex
					/*
					????????,?:??????
					Color[0]:?????
					Color[1]:????????,R?
					Color[2]:????????,G?
					Color[3]:????????,B?
					*/
					//colorI16=Color[1];
					//colorI16<<=8;colorI16&=0xF800;
					//colorI16|=((unsigned short)(Color[2]>>2)<<5);
					//colorI16|=((unsigned short)(Color[3]>>3)<<0);
					colorI16=((Color[1]>>3)<<11)|((Color[2]>>2)<<5)|((Color[3]>>3)<<0);
					//LCD_Fast_DrawPoint(colIndex,rowIndex,colorI16);
					LCD_Fast_DrawPoint(colIndex*2,rowIndex*2,colorI16);
					LCD_Fast_DrawPoint(colIndex*2+1,rowIndex*2,colorI16);
					LCD_Fast_DrawPoint(colIndex*2,rowIndex*2+1,colorI16);
					LCD_Fast_DrawPoint(colIndex*2+1,rowIndex*2+1,colorI16);
				}				
			}
		}
	}
	//if(tp_dev.touchtype&0X80)ctp_test();//电容屏测试
	//else rtp_test(); 					//电阻屏测试
}
