#! /usr/bin/env python3

import string
import textwrap
import sys
import time
import os

def emit(info, outname):
    template_comment = string.Template(
        textwrap.dedent(
            '''
            // interface "${n}" in space "${s}" '''))

    template_typename = string.Template('${p}${n}_t')
    template_typeenum = string.Template('    ${p}${n}_${m} = ${e},\n')
    template_typedef = string.Template(
        textwrap.dedent(
            '''
            typedef ${T} ${t};'''))

    template_interrupts = string.Template(
        textwrap.dedent(
            '''
            const unsigned fpga_interrupts = ${n};
            '''))

    template_reg_read = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static ${t}
            ${p}${n}_read() {
                ${t} v;
                comms_read(0, ${p}${n}_addr, 0, ${w}, ${si}, ${pad}, (char *)&v);
                return(v);
            }
            '''))

    template_reg_signed_read = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static ${t}
            ${p}${n}_read() {
                ${t} v;
                comms_read(0, ${p}${n}_addr, 0, ${w}, ${si}, ${pad}, (char *)&v);
                if ((v & ${sb}) != 0)
                    v |= ${om};
                return(v);
            }
            '''))

    template_reg_write = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static void
            ${p}${n}_write(${t} val) {
                comms_write(0, ${p}${n}_addr, 0, ${w}, (char *)&val);
            }
            '''))

    template_null_reg_write = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static void
            ${p}${n}_write() {
                comms_write(0, ${p}${n}_addr, 0, 0, NULL);
            }
            '''))




    template_queue_avail_read = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static ${t}
            ${p}${n}_read() {
                ${t} v;
                comms_read(0, ${p}${n}_addr, 0, ${w}, ${si}, ${pad}, (char *)&v);
                return(v);
            }
            '''))

    template_queue_thresh_write = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static void
            ${p}${n}_write(${t} val) {
                comms_write(0, ${p}${n}_addr, 0, ${w}, (char *)&val);
            }
            '''))

    template_mem_read = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static ${t}
            ${p}${n}_read(uint16_t mem_addr) {
                ${t} v;
                comms_read(1, ${p}${n}_addr, mem_addr, ${w}, ${si}, ${pad}, (char *)&v);
                return(v);
            }
            '''))

    template_mem_signed_read = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static ${t}
            ${p}${n}_read(uint16_t mem_addr) {
                ${t} v;
                comms_read(1, ${p}${n}_addr, mem_addr, ${w}, ${si}, ${pad}, (char *)&v);
                if ((v & ${sb}) != 0)
                    v |= ${om};
                return(v);
            }
            '''))

    template_mem_write = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static void
            ${p}${n}_write(${t} val, uint16_t mem_addr) {
                comms_write(1, ${p}${n}_addr, mem_addr, ${w}, (char *)&val);
            }
            '''))

    template_event = string.Template(
        textwrap.dedent(
            '''
            // event "${n}"
            void
            ${p}${n}_wait() {
                sem_wait(sem[${e}]);
            }
            '''))



    # emit typedef for given 3pl interface fragment
    # (also emits supplementary definitions)
    def emit_type(interface):
        # compute the next largest C primitive
        bits = interface['data_bits']
        bytes = (bits + 7) // 8
        if bits == 0:
            tokens['pad'] = 0
            return
        if bits <= 8:
            bits = 8
            tokens['pad'] = 0
        elif bits <= 16:
            bits = 16
            tokens['pad'] = 0
        elif bits <= 32:
            bits = 32
            tokens['pad'] = 4 - bytes
        else:
            bits = 64
            tokens['pad'] = 8 - bytes
        tokens['sb'] = 0

        # for constructing C bitfields
        def bitfield(bf):

            # compute padding
            pad = bits
            for f in bf:
                pad -= f['b']
            if pad != 0:
                bf.append({ 't': 'uint', 'n': '', 'b': pad})

            bs = ''
            ws = '__word'

            # generate C 
            bitfield = 'union {\n    struct {\n'
            for f in bf:
                bitfield += ('        ' + f['t'] + str(bits) + '_t ' + 
                    f['n'] + ': ' + str(f['b']) + ';\n')
            bitfield += ('    }' + bs + ';\n    ' + f['t'] +
                str(bits) + '_t ' + ws + ';\n}')
            tokens['x'] = '.' + ws
            return bitfield

        # default until we implement all
        tokens['T'] = '<<TYPE>>'
        type = interface['data_type']
        tokens['x'] = ''

        # log
        if type['type'] == 'log':
            #tokens['T'] = 'bool_t'
            tokens['T'] = 'uint8_t'

        # integer
        if type['type'] == 'int' or type['type'] == 'uint':
            tokens['T'] = type['type'] + str(bits) + '_t'
        if type['type'] == 'int':
            db = interface['data_bits']
            if db != bits:
                tokens['sb'] = hex(1 << (db - 1))
                tokens['om'] = hex((( 1 << (bits - db))-1) << db)

        # bits
        if type['type'] == 'bits':
            tokens['T'] = 'uint' + str(bits) + '_t'

        # enum
        if type['type'] == 'enum':
            tokens['T'] = 'enum {\n'
            for tokens['m'], tokens['e'] in type['values'].items():
                tokens['T'] += template_typeenum.substitute(tokens)
            tokens['T'] += '}'

        # fixed (as C bitfield)
        if type['type'] == 'fixed':
            tokens['T'] = bitfield([
                { 't': 'uint',  'n': 'frac',  'b': type['frac_bits']}, 
                { 't': 'int', 'n': 'intg', 'b': type['int_bits']}])

        if type['type'] == 'ufixed':
            tokens['T'] = bitfield([
                { 't': 'uint', 'n': 'frac',  'b': type['frac_bits']}, 
                { 't': 'uint', 'n': 'intg', 'b': type['int_bits']}])
            
        # float (as float, double, or C bitfield)
        if type['type'] == 'float':
            # special cases for standard IEEE 754 formats
            if type['mant_bits'] == 23 and type['exp_bits'] == 8:
                tokens['T'] = 'float'
            elif type['mant_bits'] == 52 and type['exp_bits'] == 11:
                tokens['T'] = 'double'
            else:
                tokens['T'] = bitfield([
                    { 't': 'uint', 'n': 'mant', 'b': type['mant_bits']}, 
                    { 't': 'uint', 'n': 'exp',  'b': type['exp_bits']},
                    { 't': 'uint', 'n': 'sign', 'b': 1 }])

        # struct (as C bitfields, 1 level deep, no arrays)
        if type['type'] == 'struct':
            fields = []
            for field in type['fields']:
                t = field['type']

                # reassign some 3pl types to ints
                if t == 'log' or t == 'enum':
                    t = 'uint' 

                # we could emit a placeholder, if we knew the bits
                if t == 'struct':
                    print('error: deep 3pl structures not supported')
                    sys.exit(1)

                fields.append({
                    't': t, 
                    'n': field['name'], 
                    'b': field['bits']})
            tokens['T'] = bitfield(fields)

        # array
        if type['type'] == 'array':
            dimension = type['dimension']
            array_type = type['arraytype']
            atype = array_type['type']
            abits = array_type['bits']
            if abits != 8 and abits != 16 and abits != 32 and abits != 64:
                print('error: array type not 8, 16, 32 or 64 bits')
                sys.exit(1)
            if atype == 'uint' or atype == 'bits':
                atype = 'uint' + str(abits) + '_t'
            elif atype == 'int':
                atype = 'int' + str(abits) + '_t'
            else:
                print('error: array type not int, uint or bits')
                sys.exit(1)
            tokens['T'] = 'struct {' + atype + ' v' + str(dimension) + ';} '

        tokens['si'] = 0
        if type['type'] == 'fixed' or type['type'] == 'int':
            tokens['si'] = 1

        tokens['t'] = template_typename.substitute(tokens)
        outfile.write(template_typedef.substitute(tokens))
        return


    with open(outname, 'w') as outfile:

        # set up substitution tokens
        tokens = {
            'p': "fpga_",
            'f': "fpga",
            'd': info['directives']['designName'],
            'z': time.strftime('%a, %d %b %Y %H:%M:%S %z'),
            'c': '',
            'f0': 'fpga_t *fpga',
            'f1': 'fpga_t *fpga, '
        }
        tokens['c'] = 'const '

        # compile lists of spaces and addresses for initialisation
        names = []
        spaces = {}
        dma_interfaces = {}

        # emit C code for each interface in each space
        scount = 0
        for sname, space in info['spaces'].items():
            tokens['s'] = sname
            if 'bus' in space:
                spaces[sname] = space['bus']
                tokens['b'] = spaces[sname]

            for iname, interface in space['interfaces'].items():
                tokens['n'] = iname

                # omit infoword/inforeset
                if iname == 'infoword' or iname == 'inforeset':
                    continue
                
                bytes = (interface['data_bits'] + 7) // 8
                if bytes == 1:
                    bits = 8
                elif bytes == 2:
                    bits = 16
                elif bytes <= 4:
                    bits = 32
                elif bytes <= 8:
                    bits = 64
                
                tokens['a'] = interface['data_addr']
                tokens['e'] = 0
                tokens['w'] = bytes

                names.append(iname)

                # emit reg/mem access address, type, and function
                outfile.write(template_comment.substitute(tokens))
                emit_type(interface)

                if sname == 'reg':
                    if "port" in interface:
                        if interface['write']:
                            outfile.write(template_mem_write.substitute(tokens))
                        else:
                            if tokens['sb'] == 0:
                                outfile.write(template_mem_read.substitute(tokens))
                            else:
                                outfile.write(template_mem_signed_read.substitute(tokens))
                    else:
                        if interface['write']:
                            if interface['data_bits'] == 0:
                                outfile.write(template_null_reg_write.substitute(tokens))
                            else:
                                outfile.write(template_reg_write.substitute(tokens))
                        else:
                            if tokens['sb'] == 0:
                                outfile.write(template_reg_read.substitute(tokens))
                            else:
                                outfile.write(template_reg_signed_read.substitute(tokens))
                    
                    if "avail_addr" in interface:
                        tokens['si'] = 0
                        tokens['pad'] = 0
                        tokens['a'] = interface['avail_addr']
                        depth = interface['data_depth']
                        check = interface['check']
                        bytes = 1
                        if not check and depth > 255:
                            bytes = 2
                        tokens['n'] = iname + '_avail'
                        tokens['T'] = 'uint' + str(bytes*8) + '_t'
                        tokens['w'] = bytes
                        tokens['t'] = template_typename.substitute(tokens)
                        outfile.write(template_comment.substitute(tokens))
                        outfile.write(template_typedef.substitute(tokens))
                        outfile.write(template_queue_avail_read.substitute(tokens))
                    
                    if "avail_thresh_addr" in interface:
                        tokens['si'] = 0
                        tokens['pad'] = 0
                        tokens['a'] = interface['avail_thresh_addr']
                        depth = interface['data_depth']
                        bytes = 1
                        if depth > 255:
                            bytes = 2
                        tokens['n'] = iname + '_avail_thresh'
                        tokens['T'] = 'uint' + str(bytes*8) + '_t'
                        tokens['w'] = bytes
                        tokens['t'] = template_typename.substitute(tokens)
                        outfile.write(template_comment.substitute(tokens))
                        outfile.write(template_typedef.substitute(tokens))
                        outfile.write(template_queue_thresh_write.substitute(tokens))
                   
        # emit C code for events
        intmax = -1
        for ename, event in info['events'].items():
            tokens['n'] = ename
            intnum = event['intr']
            tokens['e'] = intnum
            outfile.write(template_event.substitute(tokens))
            if intnum > intmax:
                intmax = intnum
        tokens['n'] = intmax + 1
        outfile.write(template_interrupts.substitute(tokens))

        return
