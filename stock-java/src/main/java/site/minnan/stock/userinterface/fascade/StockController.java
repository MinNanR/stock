package site.minnan.stock.userinterface.fascade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.minnan.stock.application.service.StockService;
import site.minnan.stock.domain.vo.EligibleStockListVO;
import site.minnan.stock.domain.vo.KLineVO;
import site.minnan.stock.domain.vo.ListQueryVO;
import site.minnan.stock.userinterface.dto.DetailsQueryDTO;
import site.minnan.stock.userinterface.dto.GetEligibleStockListDTO;
import site.minnan.stock.userinterface.response.ResponseEntity;

import javax.validation.Valid;

/**
 * 股票接口
 *
 * @author Minnan on 2022/03/29
 */
@RestController
@RequestMapping("stock")
public class StockController {

    @Autowired
    private StockService stockService;

    /**
     * 查询符合条件的股票列表
     *
     * @param dto
     * @return
     */
    @PostMapping("getEligibleStockList")
    public ResponseEntity<ListQueryVO<EligibleStockListVO>> getEligibleStockList(@RequestBody @Valid GetEligibleStockListDTO dto) throws Exception {
        ListQueryVO<EligibleStockListVO> vo = stockService.getEligibleStockList(dto);
        return ResponseEntity.success(vo);
    }

    @PostMapping("getKLineData")
    public ResponseEntity<KLineVO> getKLineData(@RequestBody @Valid DetailsQueryDTO dto){
        KLineVO vo = stockService.getKLineData(dto);
        return ResponseEntity.success(vo);
    }

}
