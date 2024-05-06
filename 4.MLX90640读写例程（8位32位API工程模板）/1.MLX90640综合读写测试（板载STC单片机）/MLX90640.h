#ifndef _MLX640_API_H
#define _MLX640_API_H

#define	MLX_Addr	0x66

void MLX90640_I2CInit();
unsigned char MLX90640_I2CRead(unsigned int startAddress, unsigned int nWordsRead, unsigned int *datas);
unsigned char MLX90640_I2CWrite(unsigned int writeAddress, unsigned int word);
signed char MLX90640_DataReady(void);
void MLX90640_SetResolution(unsigned char resolution);
unsigned char MLX90640_GetCurResolution(void);
void MLX90640_SetRefreshRate(unsigned char refreshRate);
unsigned char MLX90640_GetRefreshRate(void);
void MLX90640_SetArrangeMode(unsigned char mode);
unsigned char MLX90640_GetArrangeMode(void);
#endif
