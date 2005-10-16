/******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly 
 * marked, so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 ******************************************************************************/

#include <stdio.h>
#include <stdlib.h>

#include "dbg.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "bitset.h"

int 			completed;
int			fatal;
bitset *		procs_outstanding;

void
event_callback(dbg_event *e, void *data)
{
	stackframe *	f;
	char *		s;
	
	if (e == NULL) {
		printf("got null event!\n");
		return;
	}
	
	if (e->procs != NULL) {
		s = bitset_to_set(e->procs);
		printf("%s ", s);
		free(s);
	}
		
	printf("-> ");
	
	switch (e->event) {
	case DBGEV_ERROR:
		printf("error: %s\n", e->error_msg);
		switch (e->error_code) {
		case DBGERR_NOBACKEND:
			fatal++;
			break;
		default:
			break;
		}
		break;
	case DBGEV_OK:
		printf("command ok\n");
		break;
	case DBGEV_INIT:
		printf("debugger initilized\n");
		break;
	case DBGEV_BPHIT:
		printf("hit breakpoint %d\n", e->bpid);
		break;
	case DBGEV_BPSET:
		printf("breakpoint set\n");
		break;
	case DBGEV_STEP:
		printf("step completed\n");
		break;
	case DBGEV_SIGNAL:
		printf("received signal %s\n", e->sig_name);
		break;
	case DBGEV_EXIT:
		printf("exited with status %d\n", e->exit_status);
		break;
	case DBGEV_DATA:
		printf("data is "); AIFPrint(stdout, 0, e->data); printf("\n");
		break;
	case DBGEV_FRAMES:
		printf("got frames:\n");
		for (SetList(e->list); (f = (stackframe *)GetListElement(e->list)) != NULL; ) {
			printf(" #%d %s() at %s:%d\n", f->level, f->loc.func, f->loc.file, f->loc.line);
		}
		break;
	default:
		printf("got event %d\n", e->event);
		break;
	}

	if (e->procs != NULL) {
		bitset_invert(e->procs);
		bitset_andeq(procs_outstanding, e->procs);
		if (!bitset_isempty(procs_outstanding))
			return;
	}
	
	completed++;
}

int
wait_for_event(session *s, bitset *p)
{
	completed = 0;
	fatal = 0;
	
	if (p != NULL)
		procs_outstanding = bitset_copy(p);
		
	while (!completed) {
		if (DbgProgress(s) < 0) {
			fprintf(stderr, "error: %s\n", DbgGetErrorStr());
			exit(1);
		}
	}
	
	if (p != NULL)	
		bitset_free(procs_outstanding);
		
	/*
	 * Check a fatal error hasn't ocurred
	 */
	return fatal ? -1 : 0;
}

/*
 * Tell server to quit
 */
void
cleanup_and_exit(session *s, bitset *p)
{
	DbgQuit(s);
	wait_for_event(s, p);
	exit(0);
}

int
do_test(session *s, char *exe)
{
	bitset *		p1;
	bitset *		p2;
	int			bpid = 54;
	
	p1 = bitset_new(s->sess_procs);
	bitset_invert(p1);
	p2 = bitset_new(s->sess_procs);
	bitset_set(p2, 0);
	
	if (DbgStartSession(s, "yyy", NULL) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	if (wait_for_event(s, p1) < 0) {
		cleanup_and_exit(s, p1);
	}
		
	if (DbgStartSession(s, exe, NULL) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);

	if (DbgSetLineBreakpoint(s, p1, bpid++, "yyy.c", 6) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgSetLineBreakpoint(s, p1, bpid++, "xxx.c", 99) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgSetLineBreakpoint(s, p1, bpid, "xxx.c", 14) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgGo(s, p1) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgListStackframes(s, p1, 0) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgStep(s, p1, 1, 0) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgStep(s, p1, 1, 0) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgStep(s, p2, 1, 0) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p2);
	
	if (DbgEvaluateExpression(s, p1, "a") < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgListStackframes(s, p1, 1) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgDeleteBreakpoint(s, p1, bpid++) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
		
	if (DbgSetFuncBreakpoint(s, p1, bpid++, "xxx.c", "b") < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgGo(s, p1) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgListStackframes(s, p1, 1) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgListStackframes(s, p1, 0) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	if (DbgGo(s, p1) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);

	if (DbgQuit(s) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);

	exit(0);
}

