package apifestivos.apifestivos.servicios;

import java.util.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apifestivos.apifestivos.interfaces.IFestivoServicio;
import apifestivos.apifestivos.entidades.Festivo;
import apifestivos.apifestivos.repositorios.FestivoRepositorio;

@Service
public class FestivoServicio implements IFestivoServicio {

    @Autowired
    FestivoRepositorio repositorio;

    private Date DomingoPascua(int año) {
        int a = año % 19;
        int b = año % 4;
        int c = año % 7;
        int d = (19 * a +24) % 19;

        int dias = d + (2 * b + 4 * c + 6 * d + 5) % 7;

        int dia = 15 + dias;
        int mes = 3;
        if (dia > 31){
            dia -= 31;
            mes = 4;
        }
        return new Date(año - 1900, mes -1, dia);
    }

    private Date incrementarDias(Date fecha, int dias) {
        Calendar cld = Calendar.getInstance();
        cld.setTime(fecha);
        cld.add(Calendar.DATE, dias);
        return cld.getTime();
    }

    private Date siguienteLunes(Date fecha) {
        Calendar cld = Calendar.getInstance();
        cld.setTime(fecha);
            
        int diaSemana = cld.get(Calendar.DAY_OF_WEEK);
        if (diaSemana != Calendar.MONDAY){
            if (diaSemana > Calendar.MONDAY){
                fecha = incrementarDias(fecha, 9 - diaSemana);
            } else {
                fecha = incrementarDias(fecha, 1);
            }
        }
        return fecha;    
    }

    private List<Festivo> calcularFestivos(List<Festivo> festivos, int año) {
        if (festivos != null) {
            Date pascua = DomingoPascua(año);
            int i = 0;
            for (final Festivo festivo : festivos) {
                switch (festivo.getTipo().getId()) {
                    case 1:
                        festivo.setFecha(new Date(año - 1900, festivo.getMes() - 1, festivo.getDia()));
                        break;
                    case 2:
                        festivo.setFecha(siguienteLunes(new Date(año - 1900, festivo.getMes() - 1, festivo.getDia())));
                        break;
                    case 3:
                        festivo.setFecha(incrementarDias(pascua, festivo.getDiaspascua()));
                        break;
                    case 4:
                        festivo.setFecha(siguienteLunes(incrementarDias(pascua, festivo.getDiaspascua())));
                        break;
                }
                festivos.set(i, festivo);
                i++;
            }
        }
        return festivos;
    }

    public List<Festivo> obtenerFestivos(int año) {
        List<Festivo> festivos = repositorio.findAll();
        festivos = calcularFestivos(festivos, año);
        List<Festivo> fechas = new ArrayList<Festivo>();
        for (final Festivo festivo : festivos) {
            fechas.add(new Festivo(festivo.getFecha(), festivo.getNombre()));
        }
        return fechas;
    }

    private boolean fechasIguales(Date fecha1, Date fecha2) {
        return fecha1.getYear()==fecha2.getYear() && fecha1.getMonth()==fecha2.getMonth() && fecha1.getDay()==fecha2.getDay();
    }

    private boolean esFestivo(List<Festivo> festivos, Date fecha) {
        if (festivos != null) {
            
            festivos = calcularFestivos(festivos, fecha.getYear()+1900);

            for (final Festivo festivo : festivos) {
                Calendar c = Calendar.getInstance();
                c.setTime(fecha);
                if (fechasIguales(festivo.getFecha(), fecha) || c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
                    return true;
            }
        }
        return false;
    }
    
    public boolean esFestivo(Date fecha) {
        List<Festivo> festivos = repositorio.findAll();
        return esFestivo(festivos, fecha);
    }

    @Override
    public String verificar(int year, int month, int day){
        try {
            LocalDate localDate = LocalDate.of(year, month, day);
            Date fecha = java.sql.Date.valueOf(localDate);
            var response = esFestivo(fecha);
            return response ? "Es festivo.":"No es festivo.";
        } catch (Exception e) {
            return "Fecha no válida.";
        }
    }

    @Override
    public List<Festivo> listar() {
        return this.repositorio.findAll();
    }

    @Override
    public List<Festivo> obtener(int year) {
        return obtenerFestivos(year);
    }

}
