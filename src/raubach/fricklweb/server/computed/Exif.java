package raubach.fricklweb.server.computed;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Exif
{
	private String  apertureValue;
	private String  cameraMake;
	private String  cameraModel;
	private String  colorSpace;
	private String  compression;
	private String  compressionType;
	private String  contrast;
	private String  dataPrecision;
	private Date    dateTime;
	private Date    dateTimeOriginal;
	private Date    dateTimeDigitized;
	private String  digitalZoomRatio;
	private Integer exifImageHeight;
	private Integer exifImageWidth;
	private String  exifVersion;
	private String  exposureBiasValue;
	private String  exposureMode;
	private String  exposureProgram;
	private String  exposureTime;
	private String  fileSource;
	private String  flash;
	private String  fNumber;
	private String  focalLength;
	private String  focusType;
	private String  gainControl;
	private Double  gpsAltitude;
	private Double  gpsLatitude;
	private Double  gpsLongitude;
	private Date    gpsTimestamp;
	private Integer imageHeight;
	private Integer imageWidth;
	private Integer isoSpeedRatings;
	private String  maxExposureValue;
	private String  meteringMode;
	private String  oriantation;
	private String  photometricInterpretation;
	private String  samplesPerPixel;
	private String  saturation;
	private String  sceneCaptureType;
	private String  sceneType;
	private String  sensingMethod;
	private String  sharpness;
	private String  shutterSpeedValue;
	private String  userComment;
	private String  whiteBalanceMode;
	private String  xResolution;
	private String  yResolution;

	public String getApertureValue()
	{
		return apertureValue;
	}

	public Exif setApertureValue(String apertureValue)
	{
		this.apertureValue = apertureValue;
		return this;
	}

	public String getCameraMake()
	{
		return cameraMake;
	}

	public Exif setCameraMake(String cameraMake)
	{
		this.cameraMake = cameraMake;
		return this;
	}

	public String getCameraModel()
	{
		return cameraModel;
	}

	public Exif setCameraModel(String cameraModel)
	{
		this.cameraModel = cameraModel;
		return this;
	}

	public String getColorSpace()
	{
		return colorSpace;
	}

	public Exif setColorSpace(String colorSpace)
	{
		this.colorSpace = colorSpace;
		return this;
	}

	public String getCompression()
	{
		return compression;
	}

	public Exif setCompression(String compression)
	{
		this.compression = compression;
		return this;
	}

	public String getCompressionType()
	{
		return compressionType;
	}

	public Exif setCompressionType(String compressionType)
	{
		this.compressionType = compressionType;
		return this;
	}

	public String getContrast()
	{
		return contrast;
	}

	public Exif setContrast(String contrast)
	{
		this.contrast = contrast;
		return this;
	}

	public String getDataPrecision()
	{
		return dataPrecision;
	}

	public Exif setDataPrecision(String dataPrecision)
	{
		this.dataPrecision = dataPrecision;
		return this;
	}

	public Date getDateTime()
	{
		return dateTime;
	}

	public Exif setDateTime(Date dateTime)
	{
		this.dateTime = dateTime;
		return this;
	}

	public Date getDateTimeOriginal()
	{
		return dateTimeOriginal;
	}

	public Exif setDateTimeOriginal(Date dateTimeOriginal)
	{
		this.dateTimeOriginal = dateTimeOriginal;
		return this;
	}

	public Date getDateTimeDigitized()
	{
		return dateTimeDigitized;
	}

	public Exif setDateTimeDigitized(Date dateTimeDigitized)
	{
		this.dateTimeDigitized = dateTimeDigitized;
		return this;
	}

	public String getDigitalZoomRatio()
	{
		return digitalZoomRatio;
	}

	public Exif setDigitalZoomRatio(String digitalZoomRatio)
	{
		this.digitalZoomRatio = digitalZoomRatio;
		return this;
	}

	public Integer getExifImageHeight()
	{
		return exifImageHeight;
	}

	public Exif setExifImageHeight(Integer exifImageHeight)
	{
		this.exifImageHeight = exifImageHeight;
		return this;
	}

	public Integer getExifImageWidth()
	{
		return exifImageWidth;
	}

	public Exif setExifImageWidth(Integer exifImageWidth)
	{
		this.exifImageWidth = exifImageWidth;
		return this;
	}

	public String getExifVersion()
	{
		return exifVersion;
	}

	public Exif setExifVersion(String exifVersion)
	{
		this.exifVersion = exifVersion;
		return this;
	}

	public String getExposureBiasValue()
	{
		return exposureBiasValue;
	}

	public Exif setExposureBiasValue(String exposureBiasValue)
	{
		this.exposureBiasValue = exposureBiasValue;
		return this;
	}

	public String getExposureMode()
	{
		return exposureMode;
	}

	public Exif setExposureMode(String exposureMode)
	{
		this.exposureMode = exposureMode;
		return this;
	}

	public String getExposureProgram()
	{
		return exposureProgram;
	}

	public Exif setExposureProgram(String exposureProgram)
	{
		this.exposureProgram = exposureProgram;
		return this;
	}

	public String getExposureTime()
	{
		return exposureTime;
	}

	public Exif setExposureTime(String exposureTime)
	{
		this.exposureTime = exposureTime;
		return this;
	}

	public String getFileSource()
	{
		return fileSource;
	}

	public Exif setFileSource(String fileSource)
	{
		this.fileSource = fileSource;
		return this;
	}

	public String getFlash()
	{
		return flash;
	}

	public Exif setFlash(String flash)
	{
		this.flash = flash;
		return this;
	}

	public String getfNumber()
	{
		return fNumber;
	}

	public Exif setfNumber(String fNumber)
	{
		this.fNumber = fNumber;
		return this;
	}

	public String getFocalLength()
	{
		return focalLength;
	}

	public Exif setFocalLength(String focalLength)
	{
		this.focalLength = focalLength;
		return this;
	}

	public String getFocusType()
	{
		return focusType;
	}

	public Exif setFocusType(String focusType)
	{
		this.focusType = focusType;
		return this;
	}

	public String getGainControl()
	{
		return gainControl;
	}

	public Exif setGainControl(String gainControl)
	{
		this.gainControl = gainControl;
		return this;
	}

	public Double getGpsAltitude()
	{
		return gpsAltitude;
	}

	public Exif setGpsAltitude(Double gpsAltitude)
	{
		this.gpsAltitude = gpsAltitude;
		return this;
	}

	public Double getGpsLatitude()
	{
		return gpsLatitude;
	}

	public Exif setGpsLatitude(Double gpsLatitude)
	{
		this.gpsLatitude = gpsLatitude;
		return this;
	}

	public Double getGpsLongitude()
	{
		return gpsLongitude;
	}

	public Exif setGpsLongitude(Double gpsLongitude)
	{
		this.gpsLongitude = gpsLongitude;
		return this;
	}

	public Date getGpsTimestamp()
	{
		return gpsTimestamp;
	}

	public Exif setGpsTimestamp(Date gpsTimestamp)
	{
		this.gpsTimestamp = gpsTimestamp;
		return this;
	}

	public Integer getImageHeight()
	{
		return imageHeight;
	}

	public Exif setImageHeight(Integer imageHeight)
	{
		this.imageHeight = imageHeight;
		return this;
	}

	public Integer getImageWidth()
	{
		return imageWidth;
	}

	public Exif setImageWidth(Integer imageWidth)
	{
		this.imageWidth = imageWidth;
		return this;
	}

	public Integer getIsoSpeedRatings()
	{
		return isoSpeedRatings;
	}

	public Exif setIsoSpeedRatings(Integer isoSpeedRatings)
	{
		this.isoSpeedRatings = isoSpeedRatings;
		return this;
	}

	public String getMaxExposureValue()
	{
		return maxExposureValue;
	}

	public Exif setMaxExposureValue(String maxExposureValue)
	{
		this.maxExposureValue = maxExposureValue;
		return this;
	}

	public String getMeteringMode()
	{
		return meteringMode;
	}

	public Exif setMeteringMode(String meteringMode)
	{
		this.meteringMode = meteringMode;
		return this;
	}

	public String getOriantation()
	{
		return oriantation;
	}

	public Exif setOriantation(String oriantation)
	{
		this.oriantation = oriantation;
		return this;
	}

	public String getPhotometricInterpretation()
	{
		return photometricInterpretation;
	}

	public Exif setPhotometricInterpretation(String photometricInterpretation)
	{
		this.photometricInterpretation = photometricInterpretation;
		return this;
	}

	public String getSamplesPerPixel()
	{
		return samplesPerPixel;
	}

	public Exif setSamplesPerPixel(String samplesPerPixel)
	{
		this.samplesPerPixel = samplesPerPixel;
		return this;
	}

	public String getSaturation()
	{
		return saturation;
	}

	public Exif setSaturation(String saturation)
	{
		this.saturation = saturation;
		return this;
	}

	public String getSceneCaptureType()
	{
		return sceneCaptureType;
	}

	public Exif setSceneCaptureType(String sceneCaptureType)
	{
		this.sceneCaptureType = sceneCaptureType;
		return this;
	}

	public String getSceneType()
	{
		return sceneType;
	}

	public Exif setSceneType(String sceneType)
	{
		this.sceneType = sceneType;
		return this;
	}

	public String getSensingMethod()
	{
		return sensingMethod;
	}

	public Exif setSensingMethod(String sensingMethod)
	{
		this.sensingMethod = sensingMethod;
		return this;
	}

	public String getSharpness()
	{
		return sharpness;
	}

	public Exif setSharpness(String sharpness)
	{
		this.sharpness = sharpness;
		return this;
	}

	public String getShutterSpeedValue()
	{
		return shutterSpeedValue;
	}

	public Exif setShutterSpeedValue(String shutterSpeedValue)
	{
		this.shutterSpeedValue = shutterSpeedValue;
		return this;
	}

	public String getUserComment()
	{
		return userComment;
	}

	public Exif setUserComment(String userComment)
	{
		this.userComment = userComment;
		return this;
	}

	public String getWhiteBalanceMode()
	{
		return whiteBalanceMode;
	}

	public Exif setWhiteBalanceMode(String whiteBalanceMode)
	{
		this.whiteBalanceMode = whiteBalanceMode;
		return this;
	}

	public String getxResolution()
	{
		return xResolution;
	}

	public Exif setxResolution(String xResolution)
	{
		this.xResolution = xResolution;
		return this;
	}

	public String getyResolution()
	{
		return yResolution;
	}

	public Exif setyResolution(String yResolution)
	{
		this.yResolution = yResolution;
		return this;
	}
}
