--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
create procedure delivery_1 (
    in w_id integer,
    in carrier_id integer,
    in d_id integer)
{
  declare no_cur cursor for 
    select no_o_id
      from tpcc..new_order
      where no_w_id = w_id
        and no_d_id = d_id;

  declare _datetime timestamp;
  declare _o_id, _c_id integer; 
  declare ol_total float;

  _datetime := now ();
  open no_cur (exclusive, prefetch 1); 
  fetch no_cur into _o_id; 
  delete from tpcc..new_order where current of no_cur; 
  close no_cur; 
  
  declare o_cur cursor for 
    select o_c_id
      from tpcc..orders
      where o_w_id = w_id
        and o_d_id = d_id
	and o_id = _o_id;

  open o_cur (exclusive); 
  fetch o_cur into _c_id; 
  update tpcc..orders set
    o_carrier_id = carrier_id
    where current of o_cur; 
  close o_cur; 

  declare ol_cur cursor for 
    select ol_amount
      from tpcc..order_line
      where ol_w_id = w_id
        and ol_d_id = d_id
	and ol_o_id = _o_id; 

  ol_total := 0.0; 
  whenever not found goto lines_done; 
  open ol_cur (exclusive); 
  while (1)
    {
      declare tmp integer; 
      fetch ol_cur into tmp; 
      ol_total := ol_total + tmp; 
      update tpcc..order_line set
        ol_delivery_d = _datetime
	where current of ol_cur; 
    }

lines_done: 
  update tpcc..customer set
    c_balance = c_balance + ol_total,
    c_cnt_delivery = c_cnt_delivery + 1 
    where c_w_id = w_id and c_d_id = d_id and c_id = _c_id; 

  return _o_id;
}
