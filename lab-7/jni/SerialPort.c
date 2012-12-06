/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <errno.h>
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <linux/termios.h>

#include "android/log.h"
static const char *TAG="serial_port";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)


static speed_t
getBaudrate( jint baudrate )
{
	switch(baudrate) {
	case 0: return B0;
	case 50: return B50;
	case 75: return B75;
	case 110: return B110;
	case 134: return B134;
	case 150: return B150;
	case 200: return B200;
	case 300: return B300;
	case 600: return B600;
	case 1200: return B1200;
	case 1800: return B1800;
	case 2400: return B2400;
	case 4800: return B4800;
	case 9600: return B9600;
	case 19200: return B19200;
	case 38400: return B38400;
	case 57600: return B57600;
	case 115200: return B115200;
	case 230400: return B230400;
	case 460800: return B460800;
	case 500000: return B500000;
	case 576000: return B576000;
	case 921600: return B921600;
	case 1000000: return B1000000;
	case 1152000: return B1152000;
	case 1500000: return B1500000;
	case 2000000: return B2000000;
	case 2500000: return B2500000;
	case 3000000: return B3000000;
	case 3500000: return B3500000;
	case 4000000: return B4000000;
	default: return -1;
	}
}


/*
 * Class:     cedric_serial_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT jobject JNICALL
Java_edu_vu_isis_ammo_core_network_SerialPort_open( JNIEnv *env,
                                                    jobject thiz,
                                                    jstring path,
                                                    jint baudrate )
{
	int fd;
	speed_t speed;
	jobject mFileDescriptor;
	int customBaud = 0;

	/* Check arguments */
	{
		speed = getBaudrate(baudrate);
		if (speed == -1) {
		  LOGE("Setting up non-standard baud rate %d", baudrate);
		  speed = B38400;
		  customBaud = 1; /* we handle setting custom baud rate later */
		}
	}

	/* Opening device */
	{
		jboolean iscopy;
		const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
		LOGD("Opening serial port %s", path_utf);

		// fd = open(path_utf, O_RDWR | O_SYNC); // old version
		fd = open( path_utf, O_RDWR | O_NOCTTY | O_SYNC );
		LOGD("open() fd = %d", fd);

        int errnum = 0;
        if ( fd == -1 )
            errnum = errno;

		(*env)->ReleaseStringUTFChars(env, path, path_utf);

		if ( fd == -1 )
		{
			LOGE("Cannot open port");
			LOGE( strerror(errnum) );
			return NULL;
		}
	}

	/* Configure device */
	{
		LOGD("Configuring serial port");
		/* if (tcgetattr(fd, &cfg)) */
		/* { */
		/* 	LOGE("tcgetattr() failed"); */
		/* 	close(fd); */
		/* 	/\* TODO: throw an exception *\/ */
		/* 	return NULL; */
		/* } */

		//cfmakeraw(&cfg);

		/*  SETTING KEY:
			1 -- ignore BREAK condition
			2 -- map BREAK to SIGINTR
			3 -- mark parity and framing errors
			4 -- strip the 8th bit off chars
			5 -- map NL to CR
			6 -- ignore CR
			7 -- map CR to NL
			8 -- enable output flow control (software flow control)
			9 -- enable input flow control (software flow control)
			10-- any char will restart after stop (software flow control)
			11-- postprocess output (not set = raw output)
			12-- enable echoing of input characters
			13-- echo NL
			14-- enable canonical input (else raw)
			15-- enable SIGINTR, SIGSUSP, SIGDSUSP, and SIGQUIT signals
			16-- enable extended functions
			17-- parity enable
			18-- send 2 stop bits
			19-- character size mask
			20-- 8 bits
			21-- enable follwing output processing
		/* *\/ */

		/* //		   1      2      3      4     5      6     7    8     9    10 */
		/* cfg.c_iflag &= ~(IGNBRK|BRKINT|PARMRK|ISTRIP|INLCR|IGNCR|ICRNL|IXON|IXOFF|IXANY); */
		/* //                11 */
		/* cfg.c_oflag &= ~OPOST; */
		/* //		  12    13     14    15    16 */
    	/* 	cfg.c_lflag &= ~(ECHO|ECHONL|ICANON|ISIG|IEXTEN); */
		/* //		   17     18     19		 */
		/* cfg.c_cflag &= ~(PARENB|CSTOPB|CSIZE); */
		/* //              20 */
		/* cfg.c_cflag |= CS8; */
		/* //		  21 */
		/* cfg.c_cflag |= CRTSCTS; */


		/* cfsetispeed(&cfg, speed); */
		/* cfsetospeed(&cfg, speed); */


        ///////////////////////////////////////////////////////////////////////
        //
        // Revised version of the code
        //

		struct termios cfg;

		if (tcgetattr(fd, &cfg))
		{
			LOGE("tcgetattr() failed");
			close(fd);
			// TODO: throw an exception
			return NULL;
		}

        // Set baud rate
		cfsetispeed( &cfg, speed );
		cfsetospeed( &cfg, speed );

		cfmakeraw( &cfg );

        // Always set these
		cfg.c_cflag |= (CLOCAL | CREAD);

        // Set 8, None, 1
        cfg.c_cflag &= ~PARENB;
        cfg.c_cflag &= ~CSTOPB;
        cfg.c_cflag &= ~CSIZE;
        cfg.c_cflag |= CS8;

        // Enable hardware flow control
		cfg.c_cflag |= CRTSCTS;

        // Use raw input rather than canonical (line-oriented)
        cfg.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);

        // Disable software flow control
        cfg.c_iflag &= ~(IXON | IXOFF | IXANY);

        // Use raw output rather than processed (line-oriented)
        cfg.c_oflag &= ~OPOST;

        // Read one character at a time.  VTIME defaults to zero, so reads will
        // block indefinitely.
        cfg.c_cc[VMIN] = 0;
        cfg.c_cc[VTIME] = 5;  // Hardcoded for testing, but we should set this based on slot size.
                              // 5 means 500 ms.

        // Other "c" bits
		//cfg.c_iflag |= IGNBRK; // Ignore break condition
		cfg.c_iflag &= ~( IGNBRK | BRKINT | IGNPAR | PARMRK | INPCK | ISTRIP | INLCR | IGNCR | ICRNL | IUCLC );

        // Other "l" bits
        cfg.c_lflag &= ~IEXTEN;


        // Old, bad code. Sort of works, but was using canonical mode, which
        // we don't want.

		//struct termios config;
        //memset( &config, 0, sizeof(config) );
        //config.c_cflag = B9600 | CRTSCTS  | CS8 | CLOCAL | CREAD;
        //config.c_iflag = IGNPAR | ICRNL;
        //config.c_oflag = 0;
        //config.c_cc[VMIN] = 1;

        tcflush( fd, TCIFLUSH );

		if (tcsetattr(fd, TCSANOW, &cfg))
		{
			LOGE("tcsetattr() failed");
			close(fd);
			/* TODO: throw an exception */
			return NULL;
		}

		// for custom baud rate
		if (customBaud) 
		{
		  struct serial_struct {
		    int	type;
		    int	line;
		    unsigned int	port;
		    int	irq;
		    int	flags;
		    int	xmit_fifo_size;
		    int	custom_divisor;
		    int	baud_base;
		    unsigned short	close_delay;
		    char	io_type;
		    char	reserved_char[1];
		    int	hub6;
		    unsigned short	closing_wait; /* time to wait before closing */
		    unsigned short	closing_wait2; /* no longer used... */
		    unsigned char	*iomem_base;
		    unsigned short	iomem_reg_shift;
		    unsigned int	port_high;
		    unsigned long	iomap_base;	/* cookie passed into ioremap */
		  } sstruct;

#define TIOCGSERIAL	0x541E
#define TIOCSSERIAL	0x541F


		  if(ioctl(fd, TIOCGSERIAL, &sstruct) < 0){
		    LOGE("Error: could not get comm ioctl\n"); 
		    return NULL;
		  }
		  sstruct.custom_divisor = sstruct.baud_base / baudrate;
		  //sstruct.flags &= 0xffff ^ ASYNC_SPD_MASK; NO! makes read fail.

#define ASYNCB_SPD_HI		 4 /* Use 56000 instead of 38400 bps */
#define ASYNCB_SPD_VHI		 5 /* Use 115200 instead of 38400 bps */
#define ASYNCB_SPD_SHI		12 /* Use 230400 instead of 38400 bps */

#define ASYNC_SPD_HI		(1U << ASYNCB_SPD_HI)
#define ASYNC_SPD_VHI		(1U << ASYNCB_SPD_VHI)
#define ASYNC_SPD_SHI		(1U << ASYNCB_SPD_SHI)

#define ASYNC_SPD_CUST		(ASYNC_SPD_HI|ASYNC_SPD_VHI)
#define ASYNC_SPD_MASK		(ASYNC_SPD_HI|ASYNC_SPD_VHI|ASYNC_SPD_SHI)
		  
		  sstruct.flags &= ~ASYNC_SPD_MASK;
		  sstruct.flags |= ASYNC_SPD_CUST; 
		  if(ioctl(fd, TIOCSSERIAL, &sstruct) < 0){
		    LOGE("Error: could not set custom comm baud divisor\n"); 
		    return NULL;
		  }
		}	
	}

	/* Create a corresponding file descriptor */
	{
		jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
		jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
		jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
		mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
		(*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint)fd);
	}

	return mFileDescriptor;
}

/*
 * Class:     cedric_serial_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_edu_vu_isis_ammo_core_network_SerialPort_close( JNIEnv *env,
                                                     jobject thiz )
{
	jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
	jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

	jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
	jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");

	jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
	jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

	LOGD("close(fd = %d)", descriptor);
	close(descriptor);
}


/*
 *
 */
JNIEXPORT jint JNICALL
Java_edu_vu_isis_ammo_core_network_SerialPort_write( JNIEnv *env,
                                                     jobject thiz,
                                                     jbyteArray data )
{
    struct timeval tv;

	jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
	jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

	jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
	jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");

	jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
	jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

    jbyte *elems = (*env)->GetByteArrayElements( env, data, NULL );
    jint length = (*env)->GetArrayLength( env, data );

	LOGD( "ByteArray in JNI write(): length=%d", length );

    gettimeofday( &tv, NULL );

    ssize_t num_written = write( descriptor, elems, length );
	LOGD( "JNI: wrote bytes = %d, at time=%lu", num_written, tv.tv_sec * 1000 + tv.tv_usec / 1000 );

    (*env)->ReleaseByteArrayElements( env, data, elems, 0 );
    return 0;
}
