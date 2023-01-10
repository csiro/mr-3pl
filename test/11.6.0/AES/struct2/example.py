def fpga_SR_read ()
    # read data from FPGA - returned as a bytearray
    mem = 
    address = 
    memaddress = 
    bytes = 
    rba = comms_read (mem, address, memaddress, bytes)
    
    # convert to a single integer
    val = int.from_bytes(rba, byteorder='big', signed=False)

    # type is struct
    x = {}
        # field is "uint"
        bits = 9
        v = val & ((1 << bits) - 1)
        val = val >> bits
        x['f1'] = v

        # field is "int"
        bits = 12
        v = val & ((1 << bits) - 1)
        val = val >> bits
        if v & (1 << (bits - 1)) != 0:
            v = -((1 << bits) - v)        
        x['f2'] = v

        # field is "log"
        bits = 1
        v = val & ((1 << bits) - 1)
        val = val >> bits
        x['f3'] = v

        # field is struct
        y = {}
            # field is "uint"
            bits = 12
            v = val & ((1 << bits) - 1)
            val = val >> bits
            y['ff1'] = 

            # field is "log"
            bits = 1
            v = val & ((1 << bits) - 1)
            val = va >> bits
            y['ff2'] = 

        x['f4'] = y



        # field is "uint"
        bits = 16
        v = val & ((1 << bits) - 1)
        val = val >> bits
        x['f5'] = v

    return(x)



Type mapping to python- 
"uint"  => integer with leading zero
"int"  => integer
"log" => boolean
"ufixed" => dictionary, "frac", "int", where integer has leading zero
"fixed" => dictionary, "frac", "int"
"float" => dictionary, "frac", "exp"
"[]" => list
struct => dictionary
