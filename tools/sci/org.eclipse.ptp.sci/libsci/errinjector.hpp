#ifndef _PRAGMA_COPYRIGHT_
#define _PRAGMA_COPYRIGHT_
#pragma comment(copyright, "%Z% %I% %W% %D% %T%\0")
#endif /* _PRAGMA_COPYRIGHT_ */
/****************************************************************************

* Copyright (c) 2008, 2010 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0s
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html

 Classes: ErrorInjector

 Description: The functions of error handler include:
     a. accept user commands and sent to destination nodes
     b. prompt failure data to use with time stamp
     c. prompt recovery data to user with time stamp
   
 Author: CSH

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   05/11/09 CSH       Initial code (F156654)

****************************************************************************/

#ifndef _ERROTINJECTOR_HPP
#define _ERROTINJECTOR_HPP

#include "thread.hpp"
#include "socket.hpp"
#include <vector>

class ErrorMonitor;
class Stream;
class MessageQueue;

class ErrorInjector : public Thread 
{
    private:
        Socket              socket;
        int                 listenSockFd;
        int                 childSockFd;
        MessageQueue        *injOutErrQueue;
        ErrorMonitor        *monitor;

    public:
        ErrorInjector();
        virtual ~ErrorInjector();
        
        virtual void run();
        int stop();
        void process(Stream *stream);
        void handleCommand(std::vector<int>& numArray, int errInjType);
        void handleItem(int nodeId, int errInjType);
        
        void setInjOutQueue(MessageQueue* queue);
};

#endif

