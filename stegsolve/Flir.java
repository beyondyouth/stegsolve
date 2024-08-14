package stegsolve;

import java.io.FileInputStream;

import javax.swing.JFrame;
import java.awt.image.*;

public class Flir extends JFrame{
    /**
     * Panel containing image
     */
    private DPanel dp;
    /**
     * The image being solved
     */
    private BufferedImage bi = null;
    /**
     * The image after transformation
     */
    private FlirTransform transform = null;

    public Flir(BufferedImage b)
    {
        bi = b;
        initComponents();
        transform = new FlirTransform(bi);
        newImage();
    }

    private void initComponents() {
    }

    private void newImage()
    {
        // nowShowing.setText(transform.getText());
        dp.setImage(transform.getImage());
        dp.setSize(transform.getImage().getWidth(),transform.getImage().getHeight());
        // dp.setPreferredSize(new Dimension(transform.getImage().getWidth(),transform.getImage().getHeight()));
        this.setMaximumSize(getToolkit().getScreenSize());
        pack();
        // scrollPane.revalidate();
        repaint();
    }

    public static double raw2temp(
        double raw
    ){
        int E=1;
        int OD=1;
        int RTemp=20;
        int ATemp=20;
        int IRWTemp=20;
        int IRT=1;
        int RH=50;
        double PR1=21106.77;
        int PB=1501;
        int PF=1;
        int PO=-7340;
        double PR2=0.012545258;

        // constants
        double ATA1 = 0.006569;
        double ATA2 = 0.01262;
        double ATB1 = -0.002276;
        double ATB2 = -0.00667;
        double ATX = 1.9;

        //  transmission through window (calibrated)
        int emiss_wind = 1 - IRT;
        int refl_wind = 0;

        // transmission through the air
        double h2o = (RH / 100) * Math.exp(
            1.5587
            + 0.06939 * (ATemp)
            - 0.00027816 * Math.pow(ATemp, 2)
            + 0.00000068455 * Math.pow(ATemp, 3)
        );

        double tau1 = ATX * Math.exp(-Math.sqrt(OD / 2) * (ATA1 + ATB1 * Math.sqrt(h2o))) + (1 - ATX) * Math.exp(
            -Math.sqrt(OD / 2) * (ATA2 + ATB2 * Math.sqrt(h2o))
        );

        double tau2 = ATX * Math.exp(-Math.sqrt(OD / 2) * (ATA1 + ATB1 * Math.sqrt(h2o))) + (1 - ATX) * Math.exp(
            -Math.sqrt(OD / 2) * (ATA2 + ATB2 * Math.sqrt(h2o))
        );

        // radiance from the environment

        double raw_refl1 = PR1 / (PR2 * (Math.exp(PB / (RTemp + 273.15)) - PF)) - PO;
        double raw_refl1_attn = (1 - E) / E * raw_refl1;
        double raw_atm1 = PR1 / (PR2 * (Math.exp(PB / (ATemp + 273.15)) - PF)) - PO;
        double raw_atm1_attn = (1 - tau1) / E / tau1 * raw_atm1;
        double raw_wind = PR1 / (PR2 * (Math.exp(PB / (IRWTemp + 273.15)) - PF)) - PO;
        double raw_wind_attn = emiss_wind / E / tau1 / IRT * raw_wind;
        double raw_refl2 = PR1 / (PR2 * (Math.exp(PB / (RTemp + 273.15)) - PF)) - PO;
        double raw_refl2_attn = refl_wind / E / tau1 / IRT * raw_refl2;
        double raw_atm2 = PR1 / (PR2 * (Math.exp(PB / (ATemp + 273.15)) - PF)) - PO;
        double raw_atm2_attn = (1 - tau2) / E / tau1 / IRT / tau2 * raw_atm2;

        double raw_obj = (
            raw / E / tau1 / IRT / tau2
            - raw_atm1_attn
            - raw_atm2_attn
            - raw_wind_attn
            - raw_refl1_attn
            - raw_refl2_attn
        );

        // temperature from radiance
        double temp_celcius = PB / Math.log(PR1 / (PR2 * (raw_obj + PO)) + PF) - 273.15;
        return temp_celcius;
    }
    // @staticmethod
    // def raw2temp(
    //     raw,
    //     E=1,
    //     OD=1,
    //     RTemp=20,
    //     ATemp=20,
    //     IRWTemp=20,
    //     IRT=1,
    //     RH=50,
    //     PR1=21106.77,
    //     PB=1501,
    //     PF=1,
    //     PO=-7340,
    //     PR2=0.012545258,
    // ):
    //     """
    //     convert raw values from the flir sensor to temperatures in C
    //     # this calculation has been ported to python from
    //     # https://github.com/gtatters/Thermimage/blob/master/R/raw2temp.R
    //     # a detailed explanation of what is going on here can be found there
    //     """

    //     # constants
    //     ATA1 = 0.006569
    //     ATA2 = 0.01262
    //     ATB1 = -0.002276
    //     ATB2 = -0.00667
    //     ATX = 1.9

    //     # transmission through window (calibrated)
    //     emiss_wind = 1 - IRT
    //     refl_wind = 0

    //     # transmission through the air
    //     h2o = (RH / 100) * exp(
    //         1.5587
    //         + 0.06939 * (ATemp)
    //         - 0.00027816 * (ATemp) ** 2
    //         + 0.00000068455 * (ATemp) ** 3
    //     )
    //     tau1 = ATX * exp(-sqrt(OD / 2) * (ATA1 + ATB1 * sqrt(h2o))) + (1 - ATX) * exp(
    //         -sqrt(OD / 2) * (ATA2 + ATB2 * sqrt(h2o))
    //     )
    //     tau2 = ATX * exp(-sqrt(OD / 2) * (ATA1 + ATB1 * sqrt(h2o))) + (1 - ATX) * exp(
    //         -sqrt(OD / 2) * (ATA2 + ATB2 * sqrt(h2o))
    //     )

    //     # radiance from the environment
    //     raw_refl1 = PR1 / (PR2 * (exp(PB / (RTemp + 273.15)) - PF)) - PO
    //     raw_refl1_attn = (1 - E) / E * raw_refl1
    //     raw_atm1 = PR1 / (PR2 * (exp(PB / (ATemp + 273.15)) - PF)) - PO
    //     raw_atm1_attn = (1 - tau1) / E / tau1 * raw_atm1
    //     raw_wind = PR1 / (PR2 * (exp(PB / (IRWTemp + 273.15)) - PF)) - PO
    //     raw_wind_attn = emiss_wind / E / tau1 / IRT * raw_wind
    //     raw_refl2 = PR1 / (PR2 * (exp(PB / (RTemp + 273.15)) - PF)) - PO
    //     raw_refl2_attn = refl_wind / E / tau1 / IRT * raw_refl2
    //     raw_atm2 = PR1 / (PR2 * (exp(PB / (ATemp + 273.15)) - PF)) - PO
    //     raw_atm2_attn = (1 - tau2) / E / tau1 / IRT / tau2 * raw_atm2

    //     raw_obj = (
    //         raw / E / tau1 / IRT / tau2
    //         - raw_atm1_attn
    //         - raw_atm2_attn
    //         - raw_wind_attn
    //         - raw_refl1_attn
    //         - raw_refl2_attn
    //     )

    //     # temperature from radiance
    //     temp_celcius = PB / np.log(PR1 / (PR2 * (raw_obj + PO)) + PF) - 273.15
    //     return temp_celcius
}
