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
            // interface "${n}" in space "${s}" (bus "${b}")'''))

    template_typename = string.Template('${p}${n}_t')
    template_typeenum = string.Template('    ${p}${n}_${m} = ${e},\n')
    template_typedef = string.Template(
        textwrap.dedent(
            '''
            typedef ${T} ${t};'''))

    template_reg_read = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static __inline__ ${t}
            ${p}${n}_read() {
                FPGA_REG_READ(${p}${n}_addr, ${t}, ${w});
            }
            '''))

    template_reg_write = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static __inline__ void
            ${p}${n}_write(${t} val) {
                FPGA_REG_WRITE(${p}${n}_addr, val, ${w});
            }
            '''))

    template_null_reg_write = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static __inline__ void
            ${p}${n}_write() {
                uint32_t    val = 0;
                FPGA_REG_WRITE(${p}${n}_addr, val, 1);
            }
            '''))

    template_mem_read = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static __inline__ ${t}
            ${p}${n}_read(${f1}unsigned loc) {
                FPGA_MEM_READ(${p}${n}_addr, loc, ${t}, ${w});
            }
            '''))

    template_mem_write = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static __inline__ void
            ${p}${n}_write(${f1}unsigned loc, ${p}${n}_t val) {
                FPGA_MEM_WRITE(${p}${n}_addr, loc, val, ${w});
            }
            '''))




    template_queue_avail_read = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static ${t}
            ${p}${n}_read() {
                FPGA_REG_READ(${p}${n}_addr, ${t}, ${w});
            }
            '''))

    template_queue_thresh_write = string.Template(
        textwrap.dedent(
            '''
            ${c}unsigned ${p}${n}_addr = ${a};
            static void
            ${p}${n}_write(${t} val) {
                FPGA_REG_WRITE(${p}${n}_addr, val, ${w});
            }
            '''))

    template_event = string.Template(
        textwrap.dedent(
            '''
            // event "${n}"
            static __inline__ void
            ${p}${n}_enable() {
                fpga_event_control(${e}, 1);
            }
            static __inline__ void
            ${p}${n}_disable() {
                fpga_event_control(${e}, 0);
            }
            static __inline__ void
            ${p}${n}_wait() {
                fpga_event_wait(${e});
            }
            static __inline__ int
            ${p}${n}_count() {
                return(uio_event_count(${e}));
            }
            '''))

    template_DMA_channel = string.Template(
        textwrap.dedent(
            '''
            const unsigned fpga_${id}_blocks = ${bn};
            const unsigned fpga_${id}_block_size = ${bs};
            const unsigned fpga_${id}_block_words = ${bw};
            const void *fpga_${id}_phys_addr = (void *)${pa};'''))

    template_dma_base_address = string.Template(
            '    fpga_${nm}_base_address_write(fpga, fpga->dma.phys+${bs});\n'
            )

    template_map_start = string.Template(
        textwrap.dedent(
            '''
            // space and event bitfield for fpga_map() 2nd argument
            #define ${p}map_needed \\
                fpga_map_none'''))

    template_map_space = string.Template(
        ' \\\n    | fpga_space_${s}')

    template_map_event = string.Template(
        ' \\\n    | ${p}${n}_event')

    template_map_stop = string.Template(
        '\n')


    # emit typedef for given 3pl interface fragment
    # (also emits supplementary definitions)
    def emit_type(interface):

        # compute the next largest C primitive
        if interface['data_bits'] == 0:
            return
        if interface['data_bits'] <= 8:
            bits = 8
        elif interface['data_bits'] <= 16:
            bits = 16
        elif interface['data_bits'] <= 32:
            bits = 32
        else:
            bits = 64

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
            tokens['T'] = 'bool_t'

        # integer
        if type['type'] == 'int' or type['type'] == 'uint':
            tokens['T'] = type['type'] + str(bits) + '_t'

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
                { 't': 'int',  'n': 'intg',  'b': type['int_bits']}, 
                { 't': 'uint', 'n': 'frac', 'b': type['frac_bits']}])

        if type['type'] == 'ufixed':
            tokens['T'] = bitfield([
                { 't': 'uint', 'n': 'intg',  'b': type['int_bits']}, 
                { 't': 'uint', 'n': 'frac', 'b': type['frac_bits']}])
            
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
            if abits != 32 and abits != 64:
                print('error: array type not 32 or 64 bits')
                sys.exit(1)
            if atype == 'uint' or atype == 'bits':
                atype = 'uint' + str(abits) + '_t'
            elif atype == 'int':
                atype = 'int' + str(abits) + '_t'
            else:
                print('error: array type not int, uint or bits')
                sys.exit(1)
            tokens['T'] = 'struct {' + atype + ' v' + str(dimension) + ';} '

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

        # emit C code for events
        for ename, event in info['events'].items():
            tokens['n'] = ename
            tokens['e'] = event['intr']
            outfile.write(template_event.substitute(tokens))

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

                if sname == 'dma':
                    dma_interfaces[iname] = interface
                    outfile.write(template_comment.substitute(tokens))
                    tokens['b'] = interface['bus']
                    tokens['id'] = iname
                    tokens['bn'] = interface['blocks']
                    tokens['bs'] = interface['block_size']
                    tokens['bw'] = interface['block_words']
                    tokens['pa'] = hex(int(interface['phys_addr']))
                    outfile.write(template_DMA_channel.substitute(tokens))
                    emit_type(interface)
                    outfile.write("\n")
                else:
                    tokens['a'] = interface['data_addr']
                    tokens['e'] = 0
                    tokens['w'] = (interface['data_bits'] - 1) // 32 + 1

                    names.append(iname)

                    # emit reg/mem access address, type, and function
                    outfile.write(template_comment.substitute(tokens))
                    emit_type(interface)

                    if sname == 'reg':
                        if interface['write']:
                            if interface['data_bits'] == 0:
                                outfile.write(
                                    template_null_reg_write.substitute(tokens))
                            else:
                                outfile.write(
                                    template_reg_write.substitute(tokens))
                        else:
                            outfile.write(template_reg_read.substitute(tokens))
                    elif sname == 'mem':
                        if interface['write']:
                            outfile.write(template_mem_write.substitute(tokens))
                        else:
                            outfile.write(template_mem_read.substitute(tokens))


                    # emit additional ones for queues
                    if not 'queue' in interface['interface']:
                        continue

                    # available count/thresh
                    t = tokens['t']
                    tokens['t'] = 'uint32_t'
                    tokens['w'] = 1

                    if 'avail_addr' in interface:
                        tokens['n'] = iname + '_avail'
                        tokens['a'] = interface['avail_addr']
                        outfile.write(template_reg_read.substitute(tokens))

                    if 'avail_thresh_addr' in interface:
                        tokens['n'] = iname + '_avail_thresh'
                        tokens['a'] = interface['avail_thresh_addr']
                        outfile.write(template_reg_write.substitute(tokens))


        # DMA base address initialisations
        ba = 0
        for iname, interface in dma_interfaces.items():
            tokens['bs'] = ba
            tokens['nm'] = iname
            ba = ba + interface['block_size']

        # commence emit map bitfield
        outfile.write(template_map_start.substitute(tokens))

        # space mappings by name
        for space, sname in spaces.items(): 
            tokens['s'] = space
            outfile.write(template_map_space.substitute(tokens))
        
        # event mappings by number
        for ename, event in info['events'].items():
            tokens['n'] = ename
            outfile.write(template_map_event.substitute(tokens))

        # complete init function
        outfile.write(template_map_stop.substitute(tokens))

        return
