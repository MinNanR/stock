#统计每日均价存储过程
drop procedure if exists calculate_avg_price;
create procedure calculate_avg_price(today varchar(50))
begin
    declare flag int default 0;
    declare dataId int;
    declare stockId int;
    declare avgEndPrice decimal(10, 4);
    declare lastEndPrice decimal(10, 4);
    declare avgEndPriceLast decimal(10, 4);

    declare cur cursor for select id, stock_id from stock_price_history where note_date = today;
    declare continue handler for not found set flag = 1;
    open cur;
    fetch cur into dataId,stockId;
    while flag != 1
        do
            select avg(t1.end_price), max(t2.end_price), max(t2.avg_price_past_120_days)
            into avgEndPrice, lastEndPrice, avgEndPriceLast
            from (select end_price
                  from stock_price_history
                  where stock_id = stockId
                  order by note_date desc
                  limit 120) t1,
                 (select end_price, avg_price_past_120_days
                  from stock_price_history
                  where stock_id = stockId
                    and note_date < today
                  order by note_date desc
                  limit 1) t2;
            update stock_price_history
            set avg_price_past_120_days      = avgEndPrice,
                end_price_last               = lastEndPrice,
                avg_price_past_120_days_last = avgEndPriceLast
            where id = dataId;
            fetch cur into dataId,stockId;
        end while;
    close cur;

end;
call calculate_avg_price('2021-11-26');
drop procedure calculate_avg_price;